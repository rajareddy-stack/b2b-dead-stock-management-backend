package com.deadstock.marketplace.service;

import com.deadstock.marketplace.dto.AdminStatsDto;
import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.OfferStatus;
import com.deadstock.marketplace.model.enums.Role;
import com.deadstock.marketplace.model.enums.VerificationStatus;
import com.deadstock.marketplace.repository.BusinessProfileRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.repository.OfferRepository;
import com.deadstock.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ListingRepository listingRepository;
    private final OfferRepository offerRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        BusinessProfileRepository businessProfileRepository,
                        ListingRepository listingRepository,
                        OfferRepository offerRepository,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.listingRepository = listingRepository;
        this.offerRepository = offerRepository;
        this.notificationService = notificationService;
    }

    public List<BusinessProfile> getPendingVerifications() {
        return businessProfileRepository.findByVerificationStatus(VerificationStatus.PENDING);
    }

    public List<BusinessProfile> getAllBusinesses() {
        return businessProfileRepository.findAll();
    }

    @Transactional
    public void verifyBusiness(Long companyId) {
        BusinessProfile profile = businessProfileRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with ID: " + companyId));

        profile.setVerificationStatus(VerificationStatus.APPROVED);
        businessProfileRepository.save(profile);

        notificationService.sendNotification(
                profile.getUser(),
                "Business Verification Approved",
                "Congratulations! Your business account (" + profile.getCompanyName() + ") has been officially verified by Admin.",
                "/profile"
        );
    }

    @Transactional
    public void rejectBusiness(Long companyId) {
        BusinessProfile profile = businessProfileRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with ID: " + companyId));

        profile.setVerificationStatus(VerificationStatus.REJECTED);
        businessProfileRepository.save(profile);

        notificationService.sendNotification(
                profile.getUser(),
                "Business Verification Update",
                "Your business account verification was rejected. Please review tax ID and company details in your profile.",
                "/profile"
        );
    }

    public AdminStatsDto getAdminStats() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalSellers(userRepository.countByRole(Role.SELLER));
        stats.setTotalBuyers(userRepository.countByRole(Role.BUYER));
        stats.setTotalListings(listingRepository.count());
        
        BigDecimal surplusVal = listingRepository.calculateTotalActiveSurplusValue();
        stats.setTotalActiveSurplusValue(surplusVal != null ? surplusVal : BigDecimal.ZERO);

        stats.setTotalOffers(offerRepository.count());
        stats.setAcceptedDealsCount(offerRepository.countByStatus(OfferStatus.ACCEPTED));

        BigDecimal dealVol = offerRepository.calculateTotalAcceptedDealVolume();
        stats.setTotalAcceptedDealVolume(dealVol != null ? dealVol : BigDecimal.ZERO);

        stats.setPendingVerifications(businessProfileRepository.countByVerificationStatus(VerificationStatus.PENDING));

        return stats;
    }
}
