package com.deadstock.marketplace.repository;

import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findBySellerOrderByCreatedAtDesc(User seller);
    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<Listing> findByCategoryAndStatusOrderByCreatedAtDesc(Category category, ListingStatus status);

    @Query("SELECT DISTINCT l FROM Listing l LEFT JOIN l.tags tag WHERE l.status = :status AND " +
           "(:categoryId = 0 OR l.category.id = :categoryId) AND " +
           "LOWER(l.location) LIKE LOWER(:locationPattern) AND " +
           "(LOWER(l.title) LIKE LOWER(:queryPattern) OR LOWER(l.description) LIKE LOWER(:queryPattern) " +
           "OR LOWER(tag) LIKE LOWER(:queryPattern)) " +
           "ORDER BY l.createdAt DESC")
    List<Listing> searchListings(@Param("status") ListingStatus status,
                                 @Param("categoryId") Long categoryId,
                                 @Param("locationPattern") String locationPattern,
                                 @Param("queryPattern") String queryPattern);

    long countByStatus(ListingStatus status);

    @Query("SELECT COALESCE(SUM(l.price * l.quantity), 0) FROM Listing l WHERE l.status = 'AVAILABLE'")
    BigDecimal calculateTotalActiveSurplusValue();

    @Query("SELECT DISTINCT l.location FROM Listing l WHERE l.status = 'AVAILABLE'")
    List<String> findDistinctLocations();
}
