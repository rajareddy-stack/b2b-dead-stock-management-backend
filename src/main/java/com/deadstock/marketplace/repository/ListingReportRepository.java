package com.deadstock.marketplace.repository;

import com.deadstock.marketplace.model.ListingReport;
import com.deadstock.marketplace.model.enums.ListingReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingReportRepository extends JpaRepository<ListingReport, Long> {
    List<ListingReport> findByStatusOrderByCreatedAtAsc(ListingReportStatus status);

    boolean existsByReporter_IdAndListing_IdAndStatus(
            Long reporterId, Long listingId, ListingReportStatus status);

    boolean existsByListing_Id(Long listingId);
}
