package com.deadstock.marketplace.controller.api;

import com.deadstock.marketplace.dto.CounterOfferForm;
import com.deadstock.marketplace.dto.OfferForm;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.OfferService;
import com.deadstock.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Offer & Negotiation API", description = "Endpoints for submitting and negotiating surplus purchases")
public class OfferApiController {

    private final OfferService offerService;
    private final UserService userService;

    public OfferApiController(OfferService offerService, UserService userService) {
        this.offerService = offerService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Submit purchase offer / negotiation proposal")
    public ResponseEntity<?> submitOffer(@Valid @RequestBody OfferForm form) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        try {
            Offer offer = offerService.createOffer(form, currentUser);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Offer submitted successfully");
            response.put("offerId", offer.getId());
            response.put("status", offer.getStatus().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/counter")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Seller proposes counter-offer price")
    public ResponseEntity<?> counterOffer(@PathVariable Long id, @Valid @RequestBody CounterOfferForm form) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        try {
            Offer countered = offerService.sellerMakeCounterOffer(id, form, currentUser);
            return ResponseEntity.ok(Map.of("message", "Counter-offer submitted", "status", countered.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @Operation(summary = "Accept negotiation offer and confirm deal")
    public ResponseEntity<?> acceptOffer(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        try {
            Offer offer = offerService.getOfferById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));

            Offer accepted;
            if (offer.getListing().getSeller().getId().equals(currentUser.getId())) {
                accepted = offerService.acceptOfferBySeller(id, currentUser);
            } else {
                accepted = offerService.acceptCounterOfferByBuyer(id, currentUser);
            }
            return ResponseEntity.ok(Map.of("message", "Deal accepted and confirmed", "status", accepted.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @Operation(summary = "Decline/reject offer")
    public ResponseEntity<?> rejectOffer(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        try {
            Offer rejected = offerService.rejectOffer(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "Offer rejected", "status", rejected.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
