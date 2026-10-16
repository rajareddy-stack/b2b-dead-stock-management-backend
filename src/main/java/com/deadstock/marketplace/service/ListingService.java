package com.deadstock.marketplace.service;

import com.deadstock.marketplace.dto.ListingForm;
import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.repository.CategoryRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.repository.ListingReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingReportRepository listingReportRepository;
    private final CategoryRepository categoryRepository;

    public ListingService(ListingRepository listingRepository, ListingReportRepository listingReportRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.listingReportRepository = listingReportRepository;
        this.categoryRepository = categoryRepository;
    }

    private String normalizeSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        return sku.trim().toUpperCase();
    }

    private String resolveSku(String sku) {
        return sku == null || sku.isBlank()
                ? "LOT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase()
                : normalizeSku(sku);
    }

    public List<Listing> getAllActiveListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
    }

    public List<Listing> searchListings(Long categoryId, String location, String query) {
        long categoryParam = categoryId == null ? 0L : categoryId;
        String locationPattern = pattern(location);
        String queryPattern = pattern(query);
        return listingRepository.searchListings(ListingStatus.AVAILABLE, categoryParam, locationPattern, queryPattern);
    }

    private String pattern(String value) {
        return value == null || value.isBlank() ? "%" : "%" + value.trim() + "%";
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return new LinkedHashSet<>();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .map(tag -> tag.substring(0, 1).toUpperCase() + tag.substring(1))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<Listing> getListingsBySeller(User seller) {
        return listingRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    public Optional<Listing> getListingById(Long id) {
        return listingRepository.findById(id);
    }

    @Transactional
    public Listing createListing(ListingForm form, User seller) {
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category selected"));

        String image = (form.getImageUrl() != null && !form.getImageUrl().trim().isEmpty()) 
                ? form.getImageUrl() 
                : "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80";

        Listing listing = new Listing(
                seller,
                category,
                form.getTitle(),
                form.getDescription(),
                form.getOriginalPrice(),
                form.getPrice(),
                form.getQuantity(),
                form.getUnit(),
                form.getCondition(),
                form.getLocation(),
                image
        );
        listing.setSku(resolveSku(form.getSku()));
        listing.setExpiryDate(form.getExpiryDate());
        listing.setTags(normalizeTags(form.getTags()));

        return listingRepository.save(listing);
    }

    @Transactional
    public List<Listing> createListings(List<ListingForm> forms, User seller) {
        if (forms == null || forms.isEmpty() || forms.size() > 100) {
            throw new IllegalArgumentException("Bulk intake requires between 1 and 100 listings");
        }
        Set<String> skus = new LinkedHashSet<>();
        for (ListingForm form : forms) {
            if (form.getSku() == null || form.getSku().isBlank()) {
                throw new IllegalArgumentException("Each imported listing must have a SKU");
            }
            String sku = normalizeSku(form.getSku());
            if (!skus.add(sku)) {
                throw new IllegalArgumentException("Duplicate SKU in import: " + sku);
            }
            if (listingRepository.existsBySku(sku)) {
                throw new IllegalArgumentException("SKU already exists: " + sku);
            }
        }
        return forms.stream().map(form -> createListing(form, seller)).toList();
    }

    @Transactional
    public Listing updateListing(Long id, ListingForm form, User seller) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (!listing.getSeller().getId().equals(seller.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to modify this listing");
        }
        if (form.getSku() != null && !form.getSku().isBlank()
                && listingRepository.existsBySkuAndIdNot(normalizeSku(form.getSku()), id)) {
            throw new IllegalArgumentException("SKU already exists: " + normalizeSku(form.getSku()));
        }

        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category selected"));

        listing.setTitle(form.getTitle());
        listing.setDescription(form.getDescription());
        listing.setCategory(category);
        listing.setOriginalPrice(form.getOriginalPrice());
        listing.setPrice(form.getPrice());
        listing.setQuantity(form.getQuantity());
        listing.setUnit(form.getUnit());
        listing.setCondition(form.getCondition());
        listing.setLocation(form.getLocation());
        if (form.getSku() != null && !form.getSku().isBlank()) {
            listing.setSku(normalizeSku(form.getSku()));
        }
        listing.setExpiryDate(form.getExpiryDate());
        listing.setTags(normalizeTags(form.getTags()));
        if (form.getImageUrl() != null && !form.getImageUrl().trim().isEmpty()) {
            listing.setImageUrl(form.getImageUrl());
        }

        return listingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(Long id, User user, boolean isAdmin) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (!isAdmin && !listing.getSeller().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to delete this listing");
        }

        if (listingReportRepository.existsByListing_Id(id)) {
            listing.setStatus(ListingStatus.DELISTED);
            listingRepository.save(listing);
        } else {
            listingRepository.delete(listing);
        }
    }

    @Transactional
    public void updateListingStatus(Long id, ListingStatus newStatus) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));
        listing.setStatus(newStatus);
        listingRepository.save(listing);
    }

    public List<String> getDistinctLocations() {
        return listingRepository.findDistinctLocations();
    }

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }
}
