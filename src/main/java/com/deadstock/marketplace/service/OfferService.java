package com.deadstock.marketplace.service;

import com.deadstock.marketplace.dto.CounterOfferForm;
import com.deadstock.marketplace.dto.OfferForm;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.OfferStatus;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final ListingRepository listingRepository;
    private final NotificationService notificationService;

    public OfferService(OfferRepository offerRepository,
                        ListingRepository listingRepository,
                        NotificationService notificationService) {
        this.offerRepository = offerRepository;
        this.listingRepository = listingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Offer createOffer(OfferForm form, User buyer) {
        Listing listing = listingRepository.findById(form.getListingId())
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + form.getListingId()));

        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("You cannot make an offer on your own listing!");
        }

        if (listing.getStatus() != ListingStatus.AVAILABLE && listing.getStatus() != ListingStatus.UNDER_NEGOTIATION) {
            throw new IllegalStateException("This listing is no longer available for offers.");
        }

        if (form.getQuantity() > listing.getQuantity()) {
            throw new IllegalArgumentException("Offer quantity (" + form.getQuantity() + 
                    ") exceeds available quantity (" + listing.getQuantity() + ")");
        }

        Offer offer = new Offer(
                listing,
                buyer,
                form.getOfferPrice(),
                form.getQuantity(),
                form.getBuyerMessage()
        );

        Offer savedOffer = offerRepository.save(offer);

        // Mark listing as UNDER_NEGOTIATION
        listing.setStatus(ListingStatus.UNDER_NEGOTIATION);
        listingRepository.save(listing);

        // Notify Seller
        notificationService.sendNotification(
                listing.getSeller(),
                "New Offer on: " + listing.getTitle(),
                buyer.getFullName() + " made an offer of $" + form.getOfferPrice() + "/unit for " + 
                form.getQuantity() + " " + listing.getUnit(),
                "/seller/offers"
        );

        return savedOffer;
    }

    @Transactional
    public Offer sellerMakeCounterOffer(Long offerId, CounterOfferForm form, User seller) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found with ID: " + offerId));

        if (!offer.getListing().getSeller().getId().equals(seller.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to counter this offer");
        }

        offer.setCounterPrice(form.getCounterPrice());
        offer.setSellerMessage(form.getSellerMessage());
        offer.setStatus(OfferStatus.COUNTER_OFFERED);
        offer.setUpdatedAt(LocalDateTime.now());

        Offer saved = offerRepository.save(offer);

        // Notify Buyer
        notificationService.sendNotification(
                offer.getBuyer(),
                "Counter-Offer on: " + offer.getListing().getTitle(),
                seller.getFullName() + " proposed a counter-offer of $" + form.getCounterPrice() + "/unit.",
                "/buyer/dashboard"
        );

        return saved;
    }

    @Transactional
    public Offer acceptOfferBySeller(Long offerId, User seller) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found with ID: " + offerId));

        if (!offer.getListing().getSeller().getId().equals(seller.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to accept this offer");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setUpdatedAt(LocalDateTime.now());

        // Update listing status if fully acquired
        Listing listing = offer.getListing();
        if (offer.getQuantity() >= listing.getQuantity()) {
            listing.setStatus(ListingStatus.SOLD);
        } else {
            listing.setQuantity(listing.getQuantity() - offer.getQuantity());
            listing.setStatus(ListingStatus.AVAILABLE);
        }
        listingRepository.save(listing);

        Offer saved = offerRepository.save(offer);

        // Notify Buyer
        notificationService.sendNotification(
                offer.getBuyer(),
                "Deal Accepted! Listing: " + listing.getTitle(),
                "Congratulations! The seller accepted your offer for " + offer.getQuantity() + " " + listing.getUnit() + 
                " at $" + offer.getOfferPrice() + "/unit. Deal Confirmed!",
                "/buyer/dashboard"
        );

        return saved;
    }

    @Transactional
    public Offer acceptCounterOfferByBuyer(Long offerId, User buyer) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found with ID: " + offerId));

        if (!offer.getBuyer().getId().equals(buyer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to accept this counter-offer");
        }

        if (offer.getStatus() != OfferStatus.COUNTER_OFFERED) {
            throw new IllegalStateException("Only counter-offered deals can be accepted by the buyer.");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setUpdatedAt(LocalDateTime.now());

        Listing listing = offer.getListing();
        if (offer.getQuantity() >= listing.getQuantity()) {
            listing.setStatus(ListingStatus.SOLD);
        } else {
            listing.setQuantity(listing.getQuantity() - offer.getQuantity());
            listing.setStatus(ListingStatus.AVAILABLE);
        }
        listingRepository.save(listing);

        Offer saved = offerRepository.save(offer);

        // Notify Seller
        notificationService.sendNotification(
                listing.getSeller(),
                "Counter-Offer Accepted! Listing: " + listing.getTitle(),
                buyer.getFullName() + " accepted your counter-offer of $" + offer.getCounterPrice() + "/unit. Deal Confirmed!",
                "/seller/offers"
        );

        return saved;
    }

    @Transactional
    public Offer rejectOffer(Long offerId, User user) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found with ID: " + offerId));

        boolean isSeller = offer.getListing().getSeller().getId().equals(user.getId());
        boolean isBuyer = offer.getBuyer().getId().equals(user.getId());

        if (!isSeller && !isBuyer) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized to reject this offer");
        }

        offer.setStatus(OfferStatus.REJECTED);
        offer.setUpdatedAt(LocalDateTime.now());

        Offer saved = offerRepository.save(offer);

        if (isSeller) {
            notificationService.sendNotification(
                    offer.getBuyer(),
                    "Offer Declined: " + offer.getListing().getTitle(),
                    "The seller declined your offer of $" + offer.getOfferPrice() + "/unit.",
                    "/buyer/dashboard"
            );
        } else {
            notificationService.sendNotification(
                    offer.getListing().getSeller(),
                    "Counter-Offer Declined: " + offer.getListing().getTitle(),
                    "The buyer declined your counter-offer of $" + offer.getCounterPrice() + "/unit.",
                    "/seller/offers"
            );
        }

        return saved;
    }

    public List<Offer> getOffersByBuyer(User buyer) {
        return offerRepository.findByBuyerOrderByUpdatedAtDesc(buyer);
    }

    public List<Offer> getIncomingOffersForSeller(User seller) {
        return offerRepository.findIncomingOffersBySeller(seller);
    }

    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }
}
