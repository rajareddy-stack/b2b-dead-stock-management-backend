package com.deadstock.marketplace.service;

import com.deadstock.marketplace.dto.RegisterRequest;
import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.Role;
import com.deadstock.marketplace.model.enums.VerificationStatus;
import com.deadstock.marketplace.repository.BusinessProfileRepository;
import com.deadstock.marketplace.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BusinessProfileRepository businessProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists: " + request.getEmail());
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getRole()
        );

        User savedUser = userRepository.save(user);

        // Auto-approve ADMIN, set BUYER and SELLER to PENDING
        VerificationStatus initialStatus = request.getRole() == Role.ADMIN 
                ? VerificationStatus.APPROVED 
                : VerificationStatus.PENDING;

        BusinessProfile profile = new BusinessProfile(
                savedUser,
                request.getCompanyName(),
                request.getTaxId(),
                request.getAddress(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getPhone(),
                request.getIndustryType(),
                initialStatus
        );

        businessProfileRepository.save(profile);
        savedUser.setBusinessProfile(profile);

        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @Transactional
    public void updateBusinessProfile(Long userId, String companyName, String taxId, 
                                      String address, String city, String state, 
                                      String postalCode, String phone, String industryType) {
        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));

        profile.setCompanyName(companyName);
        profile.setTaxId(taxId);
        profile.setAddress(address);
        profile.setCity(city);
        profile.setState(state);
        profile.setPostalCode(postalCode);
        profile.setPhone(phone);
        profile.setIndustryType(industryType);
        businessProfileRepository.save(profile);
    }
}
