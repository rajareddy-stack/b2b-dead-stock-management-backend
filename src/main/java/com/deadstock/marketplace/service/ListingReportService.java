package com.deadstock.marketplace.service;

import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.ListingReport;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingReportStatus;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.repository.ListingReportRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListingReportService {

    private final ListingReportRepository reportRepository;
    private final ListingRepository listingRepository;

    public ListingReportService(ListingReportRepository reportRepository, ListingRepository listingRepository) {
        this.reportRepository = reportRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public ListingReport reportListing(Long listingId, User reporter, String reason) {
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isEmpty() || normalizedReason.length() > 1000) {
            throw new IllegalArgumentException("Report reason must be between 1 and 1000 characters");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (listing.getSeller().getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("You cannot report your own listing");
        }
        if (listing.getStatus() != ListingStatus.AVAILABLE && listing.getStatus() != ListingStatus.UNDER_NEGOTIATION) {
            throw new IllegalStateException("Only active listings can be reported");
        }
        if (reportRepository.existsByReporter_IdAndListing_IdAndStatus(
                reporter.getId(), listingId, ListingReportStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending report for this listing");
        }

        return reportRepository.save(new ListingReport(listing, reporter, normalizedReason));
    }

    @Transactional(readOnly = true)
    public List<ListingReport> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtAsc(ListingReportStatus.PENDING);
    }

    @Transactional
    public ListingReport dismissReport(Long reportId) {
        ListingReport report = getPendingReport(reportId);
        report.setStatus(ListingReportStatus.DISMISSED);
        return reportRepository.save(report);
    }

    @Transactional
    public ListingReport delistReportedListing(Long reportId) {
        ListingReport report = getPendingReport(reportId);
        Listing listing = report.getListing();
        listing.setStatus(ListingStatus.DELISTED);
        listingRepository.save(listing);
        report.setStatus(ListingReportStatus.DELISTED);
        return reportRepository.save(report);
    }

    private ListingReport getPendingReport(Long reportId) {
        ListingReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (report.getStatus() != ListingReportStatus.PENDING) {
            throw new IllegalStateException("This report has already been reviewed");
        }
        return report;
    }
}
