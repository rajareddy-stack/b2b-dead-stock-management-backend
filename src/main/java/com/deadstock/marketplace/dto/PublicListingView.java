package com.deadstock.marketplace.dto;

import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ItemCondition;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record PublicListingView(
        Long id,
        String title,
        String sku,
        String description,
        BigDecimal originalPrice,
        BigDecimal price,
        Integer quantity,
        String unit,
        ItemCondition condition,
        ListingStatus status,
        String location,
        String imageUrl,
        LocalDate expiryDate,
        Set<String> tags,
        LocalDateTime createdAt,
        int discountPercentage,
        PublicCategoryView category,
        PublicSellerView seller
) {

    public static PublicListingView from(Listing listing) {
        return new PublicListingView(
                listing.getId(),
                listing.getTitle(),
                listing.getSku(),
                listing.getDescription(),
                listing.getOriginalPrice(),
                listing.getPrice(),
                listing.getQuantity(),
                listing.getUnit(),
                listing.getCondition(),
                listing.getStatus(),
                listing.getLocation(),
                listing.getImageUrl(),
                listing.getExpiryDate(),
                listing.getTags(),
                listing.getCreatedAt(),
                listing.getDiscountPercentage(),
                PublicCategoryView.from(listing.getCategory()),
                PublicSellerView.from(listing.getSeller())
        );
    }

    public record PublicCategoryView(Long id, String title, String description, String icon) {
        private static PublicCategoryView from(Category category) {
            return category == null ? null : new PublicCategoryView(
                    category.getId(), category.getTitle(), category.getDescription(), category.getIcon());
        }
    }

    public record PublicSellerView(Long id, String fullName, PublicBusinessView businessProfile) {
        private static PublicSellerView from(User seller) {
            return seller == null ? null : new PublicSellerView(
                    seller.getId(),
                    seller.getFullName(),
                    PublicBusinessView.from(seller.getBusinessProfile())
            );
        }
    }

    public record PublicBusinessView(
            String companyName,
            String city,
            String state,
            String industryType,
            VerificationStatus verificationStatus
    ) {
        private static PublicBusinessView from(BusinessProfile profile) {
            return profile == null ? null : new PublicBusinessView(
                    profile.getCompanyName(),
                    profile.getCity(),
                    profile.getState(),
                    profile.getIndustryType(),
                    profile.getVerificationStatus()
            );
        }
    }
}
