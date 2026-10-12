package com.deadstock.marketplace.service;

import com.deadstock.marketplace.dto.ListingForm;
import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.repository.CategoryRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

    public ListingService(ListingRepository listingRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Listing> getAllActiveListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
    }

    public List<Listing> searchListings(Long categoryId, String location, String query) {
        String locParam = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
        String queryParam = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return listingRepository.searchListings(ListingStatus.AVAILABLE, categoryId, locParam, queryParam);
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

        return listingRepository.save(listing);
    }

    @Transactional
    public Listing updateListing(Long id, ListingForm form, User seller) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        if (!listing.getSeller().getId().equals(seller.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to modify this listing");
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

        listingRepository.delete(listing);
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
