package com.deadstock.marketplace.dto;

import java.math.BigDecimal;

public class AdminStatsDto {

    private long totalUsers;
    private long totalSellers;
    private long totalBuyers;
    private long totalListings;
    private BigDecimal totalActiveSurplusValue;
    private long totalOffers;
    private long acceptedDealsCount;
    private BigDecimal totalAcceptedDealVolume;
    private long pendingVerifications;

    public AdminStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public long getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(long totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(long totalListings) {
        this.totalListings = totalListings;
    }

    public BigDecimal getTotalActiveSurplusValue() {
        return totalActiveSurplusValue;
    }

    public void setTotalActiveSurplusValue(BigDecimal totalActiveSurplusValue) {
        this.totalActiveSurplusValue = totalActiveSurplusValue;
    }

    public long getTotalOffers() {
        return totalOffers;
    }

    public void setTotalOffers(long totalOffers) {
        this.totalOffers = totalOffers;
    }

    public long getAcceptedDealsCount() {
        return acceptedDealsCount;
    }

    public void setAcceptedDealsCount(long acceptedDealsCount) {
        this.acceptedDealsCount = acceptedDealsCount;
    }

    public BigDecimal getTotalAcceptedDealVolume() {
        return totalAcceptedDealVolume;
    }

    public void setTotalAcceptedDealVolume(BigDecimal totalAcceptedDealVolume) {
        this.totalAcceptedDealVolume = totalAcceptedDealVolume;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }
}
