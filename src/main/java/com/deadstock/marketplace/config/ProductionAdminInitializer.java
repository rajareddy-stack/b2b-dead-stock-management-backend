package com.deadstock.marketplace.config;

import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.Role;
import com.deadstock.marketplace.model.enums.VerificationStatus;
import com.deadstock.marketplace.repository.BusinessProfileRepository;
import com.deadstock.marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class ProductionAdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductionAdminInitializer.class);

    private final Environment environment;
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public ProductionAdminInitializer(Environment environment,
                                      UserRepository userRepository,
                                      BusinessProfileRepository businessProfileRepository,
                                      PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = environment.getProperty("INITIAL_ADMIN_EMAIL", "").trim();
        String password = environment.getProperty("INITIAL_ADMIN_PASSWORD", "");
        if (email.isBlank() || password.isBlank()) {
            logger.warn("Initial admin not created; configure INITIAL_ADMIN_EMAIL and INITIAL_ADMIN_PASSWORD.");
            return;
        }
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = userRepository.save(new User(
                email,
                passwordEncoder.encode(password),
                environment.getProperty("INITIAL_ADMIN_FULL_NAME", "Marketplace Administrator"),
                Role.ADMIN
        ));
        businessProfileRepository.save(new BusinessProfile(
                admin,
                environment.getProperty("INITIAL_ADMIN_COMPANY", "Marketplace Administration"),
                environment.getProperty("INITIAL_ADMIN_TAX_ID", "ADMIN-ACCOUNT"),
                environment.getProperty("INITIAL_ADMIN_ADDRESS", "Online"),
                environment.getProperty("INITIAL_ADMIN_CITY", "Mumbai"),
                environment.getProperty("INITIAL_ADMIN_STATE", "Maharashtra"),
                "",
                environment.getProperty("INITIAL_ADMIN_PHONE", "0000000000"),
                "Marketplace Administration",
                VerificationStatus.APPROVED
        ));
        logger.info("Created the configured initial admin account.");
    }
}
