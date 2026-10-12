package com.deadstock.marketplace.controller.api;

import com.deadstock.marketplace.dto.ListingForm;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.ListingService;
import com.deadstock.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listing API", description = "Endpoints for searching and managing dead stock surplus inventory")
public class ListingApiController {

    private final ListingService listingService;
    private final UserService userService;

    public ListingApiController(ListingService listingService, UserService userService) {
        this.listingService = listingService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Search surplus listings by location, category, or keyword")
    public ResponseEntity<List<Listing>> getListings(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(listingService.searchListings(categoryId, location, query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single listing details by ID")
    public ResponseEntity<?> getListingById(@PathVariable Long id) {
        return listingService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new dead stock listing (Requires authenticated seller)")
    public ResponseEntity<?> createListing(@Valid @RequestBody ListingForm form) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        if (currentUser.getRole() != com.deadstock.marketplace.model.enums.Role.SELLER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only registered sellers can create listings"));
        }

        try {
            Listing created = listingService.createListing(form, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Listing created successfully");
            response.put("listingId", created.getId());
            response.put("title", created.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
