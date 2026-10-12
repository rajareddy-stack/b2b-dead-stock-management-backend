package com.deadstock.marketplace.repository;

import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByBuyerOrderByUpdatedAtDesc(User buyer);
    List<Offer> findByListingOrderByUpdatedAtDesc(Listing listing);
    
    @Query("SELECT o FROM Offer o WHERE o.listing.seller = :seller ORDER BY o.updatedAt DESC")
    List<Offer> findIncomingOffersBySeller(@Param("seller") User seller);

    long countByStatus(OfferStatus status);

    @Query("SELECT COALESCE(SUM(CASE WHEN o.counterPrice IS NOT NULL THEN o.counterPrice * o.quantity ELSE o.offerPrice * o.quantity END), 0) " +
           "FROM Offer o WHERE o.status = 'ACCEPTED'")
    BigDecimal calculateTotalAcceptedDealVolume();
}
