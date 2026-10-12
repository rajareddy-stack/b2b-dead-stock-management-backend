package com.deadstock.marketplace.controller.api;

import com.deadstock.marketplace.dto.AdminStatsDto;
import com.deadstock.marketplace.dto.ListingForm;
import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.Notification;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.OfferStatus;
import com.deadstock.marketplace.service.AdminService;
import com.deadstock.marketplace.service.CategoryService;
import com.deadstock.marketplace.service.ListingService;
import com.deadstock.marketplace.service.NotificationService;
import com.deadstock.marketplace.service.OfferService;
import com.deadstock.marketplace.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarketplaceApiController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final ListingService listingService;
    private final NotificationService notificationService;
    private final OfferService offerService;
    private final UserService userService;

    public MarketplaceApiController(AdminService adminService,
                                    CategoryService categoryService,
                                    ListingService listingService,
                                    NotificationService notificationService,
                                    OfferService offerService,
                                    UserService userService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
        this.listingService = listingService;
        this.notificationService = notificationService;
        this.offerService = offerService;
        this.userService = userService;
    }

    @GetMapping("/auth/me")
    @Transactional(readOnly = true)
    public ResponseEntity<?> currentUser() {
        User user = userService.getCurrentUser();
        return user == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"))
                : ResponseEntity.ok(userView(user));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/overview")
    @Transactional(readOnly = true)
    public Map<String, Object> overview() {
        List<Listing> featured = listingService.getAllActiveListings().stream().limit(6).toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", statsView(adminService.getAdminStats()));
        result.put("categories", categoryService.getAllCategories());
        result.put("featuredListings", featured.stream().map(this::listingView).toList());
        return result;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/locations")
    public List<String> locations() {
        return listingService.getDistinctLocations();
    }

    @GetMapping("/buyer/dashboard")
    @PreAuthorize("hasRole('BUYER')")
    @Transactional(readOnly = true)
    public Map<String, Object> buyerDashboard() {
        User buyer = requireCurrentUser();
        List<Offer> offers = offerService.getOffersByBuyer(buyer);
        return Map.of(
                "offers", offers.stream().map(this::offerView).toList(),
                "acceptedOfferCount", offers.stream().filter(o -> o.getStatus() == OfferStatus.ACCEPTED).count()
        );
    }

    @GetMapping("/seller/dashboard")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public Map<String, Object> sellerDashboard() {
        User seller = requireCurrentUser();
        List<Listing> listings = listingService.getListingsBySeller(seller);
        List<Offer> offers = offerService.getIncomingOffersForSeller(seller);
        return Map.of(
                "listings", listings.stream().map(this::listingView).toList(),
                "offers", offers.stream().map(this::offerView).toList(),
                "listedStockValue", listings.stream().filter(this::isActive)
                        .map(l -> l.getPrice().multiply(java.math.BigDecimal.valueOf(l.getQuantity())))
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                "tiedUpCapital", listings.stream().filter(this::isActive)
                        .map(l -> l.getOriginalPrice().multiply(java.math.BigDecimal.valueOf(l.getQuantity())))
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                "pendingOfferCount", offers.stream().filter(o -> o.getStatus() == OfferStatus.PENDING || o.getStatus() == OfferStatus.COUNTER_OFFERED).count()
        );
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Map<String, Object> profile() {
        User user = requireCurrentUser();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", userView(user));
        result.put("businessProfile", businessView(user.getBusinessProfile()));
        return result;
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdate update) {
        User user = requireCurrentUser();
        try {
            userService.updateBusinessProfile(user.getId(), update.companyName(), update.taxId(), update.address(),
                    update.city(), update.state(), update.postalCode(), update.phone(), update.industryType());
            return ResponseEntity.ok(Map.of("message", "Business profile updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> notifications() {
        return notificationService.getNotificationsForUser(requireCurrentUser()).stream()
                .map(this::notificationView).toList();
    }

    @PostMapping("/notifications/read/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markNotificationRead(@PathVariable Long id) {
        notificationService.markAsRead(id, requireCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllNotificationsRead() {
        notificationService.markAllAsRead(requireCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/businesses")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> businesses() {
        return adminService.getAllBusinesses().stream().map(this::businessView).toList();
    }

    @GetMapping("/admin/listings")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> adminListings() {
        return listingService.getAllListings().stream().map(this::listingView).toList();
    }

    @DeleteMapping("/admin/listings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> moderateDeleteListing(@PathVariable Long id) {
        try {
            listingService.deleteListing(id, requireCurrentUser(), true);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/listings/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> updateListing(@PathVariable Long id, @Valid @RequestBody ListingForm form) {
        try {
            Listing updated = listingService.updateListing(id, form, requireCurrentUser());
            return ResponseEntity.ok(listingView(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/listings/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        try {
            listingService.deleteListing(id, requireCurrentUser(), false);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User requireCurrentUser() {
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        return user;
    }

    private boolean isActive(Listing listing) {
        return listing.getStatus() == ListingStatus.AVAILABLE || listing.getStatus() == ListingStatus.UNDER_NEGOTIATION;
    }

    private Map<String, Object> userView(User user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("email", user.getEmail());
        view.put("fullName", user.getFullName());
        view.put("role", user.getRole());
        view.put("businessProfile", businessView(user.getBusinessProfile()));
        return view;
    }

    private Map<String, Object> businessView(BusinessProfile profile) {
        if (profile == null) {
            return null;
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", profile.getId());
        view.put("companyName", profile.getCompanyName());
        view.put("taxId", profile.getTaxId());
        view.put("address", profile.getAddress());
        view.put("city", profile.getCity());
        view.put("state", profile.getState());
        view.put("postalCode", profile.getPostalCode());
        view.put("phone", profile.getPhone());
        view.put("industryType", profile.getIndustryType());
        view.put("verificationStatus", profile.getVerificationStatus());
        User owner = profile.getUser();
        if (owner != null) {
            view.put("userId", owner.getId());
            view.put("fullName", owner.getFullName());
            view.put("email", owner.getEmail());
            view.put("role", owner.getRole());
        }
        return view;
    }

    private Map<String, Object> listingView(Listing listing) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", listing.getId());
        view.put("title", listing.getTitle());
        view.put("description", listing.getDescription());
        view.put("originalPrice", listing.getOriginalPrice());
        view.put("price", listing.getPrice());
        view.put("quantity", listing.getQuantity());
        view.put("unit", listing.getUnit());
        view.put("condition", listing.getCondition());
        view.put("status", listing.getStatus());
        view.put("location", listing.getLocation());
        view.put("imageUrl", listing.getImageUrl());
        view.put("createdAt", listing.getCreatedAt());
        view.put("discountPercentage", listing.getDiscountPercentage());
        view.put("category", listing.getCategory());
        User seller = listing.getSeller();
        if (seller != null) {
            view.put("seller", Map.of("id", seller.getId(), "fullName", seller.getFullName(), "email", seller.getEmail()));
        }
        return view;
    }

    private Map<String, Object> offerView(Offer offer) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", offer.getId());
        view.put("offerPrice", offer.getOfferPrice());
        view.put("quantity", offer.getQuantity());
        view.put("counterPrice", offer.getCounterPrice());
        view.put("status", offer.getStatus());
        view.put("buyerMessage", offer.getBuyerMessage());
        view.put("sellerMessage", offer.getSellerMessage());
        view.put("createdAt", offer.getCreatedAt());
        view.put("updatedAt", offer.getUpdatedAt());
        view.put("totalAmount", offer.getTotalAmount());
        view.put("listing", listingView(offer.getListing()));
        User buyer = offer.getBuyer();
        if (buyer != null) {
            view.put("buyer", Map.of("id", buyer.getId(), "fullName", buyer.getFullName(), "email", buyer.getEmail()));
        }
        return view;
    }

    private Map<String, Object> notificationView(Notification notification) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", notification.getId());
        view.put("title", notification.getTitle());
        view.put("message", notification.getMessage());
        view.put("read", notification.isRead());
        view.put("linkUrl", notification.getLinkUrl());
        view.put("createdAt", notification.getCreatedAt());
        return view;
    }

    private Map<String, Object> statsView(AdminStatsDto stats) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("totalUsers", stats.getTotalUsers());
        view.put("totalSellers", stats.getTotalSellers());
        view.put("totalBuyers", stats.getTotalBuyers());
        view.put("totalListings", stats.getTotalListings());
        view.put("totalActiveSurplusValue", stats.getTotalActiveSurplusValue());
        view.put("totalOffers", stats.getTotalOffers());
        view.put("acceptedDealsCount", stats.getAcceptedDealsCount());
        view.put("totalAcceptedDealVolume", stats.getTotalAcceptedDealVolume());
        view.put("pendingVerifications", stats.getPendingVerifications());
        return view;
    }

    public record ProfileUpdate(
            @NotBlank String companyName,
            @NotBlank String taxId,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String state,
            String postalCode,
            @NotBlank String phone,
            String industryType) {
    }
}
