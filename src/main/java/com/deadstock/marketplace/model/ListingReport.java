package com.deadstock.marketplace.model;

import com.deadstock.marketplace.model.enums.ListingReportStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing_reports")
public class ListingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingReportStatus status = ListingReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    protected ListingReport() {
    }

    public ListingReport(Listing listing, User reporter, String reason) {
        this.listing = listing;
        this.reporter = reporter;
        this.reason = reason;
        this.status = ListingReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public User getReporter() {
        return reporter;
    }

    public String getReason() {
        return reason;
    }

    public ListingReportStatus getStatus() {
        return status;
    }

    public void setStatus(ListingReportStatus status) {
        this.status = status;
        this.resolvedAt = status == ListingReportStatus.PENDING ? null : LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
