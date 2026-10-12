package com.deadstock.marketplace.repository;

import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByUser(User user);
    Optional<BusinessProfile> findByUserId(Long userId);
    List<BusinessProfile> findByVerificationStatus(VerificationStatus status);
    long countByVerificationStatus(VerificationStatus status);
}
