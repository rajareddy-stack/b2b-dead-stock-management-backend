package com.deadstock.marketplace.config;

import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ItemCondition;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.Role;
import com.deadstock.marketplace.model.enums.VerificationStatus;
import com.deadstock.marketplace.repository.BusinessProfileRepository;
import com.deadstock.marketplace.repository.CategoryRepository;
import com.deadstock.marketplace.repository.ListingRepository;
import com.deadstock.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("production")
@Order(2)
public class ProductionSampleDataInitializer implements CommandLineRunner {

    private final boolean enabled;
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;

    public ProductionSampleDataInitializer(
            @Value("${app.marketplace.sample-data-enabled:false}") boolean enabled,
            UserRepository userRepository,
            BusinessProfileRepository businessProfileRepository,
            CategoryRepository categoryRepository,
            ListingRepository listingRepository,
            PasswordEncoder passwordEncoder) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.categoryRepository = categoryRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || userRepository.existsByEmail("sample-supplier@deadstockmarketplace.invalid")) {
            return;
        }

        User supplier = userRepository.save(new User(
                "sample-supplier@deadstockmarketplace.invalid",
                passwordEncoder.encode(UUID.randomUUID().toString()),
                "Demo Supply Co.",
                Role.SELLER
        ));
        supplier.setEnabled(false);
        businessProfileRepository.save(new BusinessProfile(
                supplier,
                "Demo Supply Co. (Sample Inventory)",
                "SAMPLE-ONLY",
                "Industrial Estate",
                "Pune",
                "Maharashtra",
                "411001",
                "0000000000",
                "Industrial Materials",
                VerificationStatus.APPROVED
        ));

        List<Listing> samples = List.of(
                listing(supplier, "Construction & Raw Metals", "Sample - Stainless steel sheet offcuts",
                        "Demo inventory: stainless steel 304 sheets, clean and dry, available for local pickup.",
                        "DEMO-STEEL-001", "Pune, Maharashtra", 8200, 5900, 24, "Sheets",
                        ItemCondition.BRAND_NEW_SURPLUS, null, Set.of("Demo sample", "Stainless steel", "Metal")),
                listing(supplier, "Packaging & Plastics", "Sample - Reusable HDPE drums",
                        "Demo inventory: unused food-grade HDPE drums with lids. Sample record, not a live offer.",
                        "DEMO-PACK-002", "Nashik, Maharashtra", 900, 540, 80, "Pieces",
                        ItemCondition.OVERSTOCKED, null, Set.of("Demo sample", "HDPE", "Packaging")),
                listing(supplier, "Industrial Machinery & Parts", "Sample - Three-phase induction motors",
                        "Demo inventory: surplus 5 HP motors, unopened, specifications available on request.",
                        "DEMO-MACH-003", "Ahmedabad, Gujarat", 28500, 19800, 6, "Units",
                        ItemCondition.BRAND_NEW_SURPLUS, null, Set.of("Demo sample", "Motors", "Electrical")),
                listing(supplier, "Textiles & Raw Fabrics", "Sample - Cotton canvas surplus rolls",
                        "Demo inventory: natural cotton canvas rolls, stored indoors. Sample record for the marketplace preview.",
                        "DEMO-TEXT-004", "Coimbatore, Tamil Nadu", 7500, 4950, 18, "Rolls",
                        ItemCondition.OVERSTOCKED, null, Set.of("Demo sample", "Cotton", "Fabric")),
                listing(supplier, "Chemicals & Coatings", "Sample - Water-based industrial coating",
                        "Demo inventory: sealed coating containers with remaining shelf life. Verify batch documents before purchase.",
                        "DEMO-CHEM-005", "Vadodara, Gujarat", 4200, 3100, 40, "Containers",
                        ItemCondition.BRAND_NEW_SURPLUS, LocalDate.now().plusMonths(8),
                        Set.of("Demo sample", "Coating", "Expiry tracked"))
        );
        listingRepository.saveAll(samples);
    }

    private Listing listing(User seller, String categoryTitle, String title, String description,
                            String sku, String location, long originalPrice, long price, int quantity,
                            String unit, ItemCondition condition, LocalDate expiryDate, Set<String> tags) {
        Category category = categoryRepository.findByTitle(categoryTitle)
                .orElseThrow(() -> new IllegalStateException("Missing marketplace category: " + categoryTitle));
        Listing listing = new Listing(
                seller, category, title, description,
                BigDecimal.valueOf(originalPrice), BigDecimal.valueOf(price), quantity, unit, condition,
                location, null
        );
        listing.setSku(sku);
        listing.setExpiryDate(expiryDate);
        listing.setTags(tags);
        listing.setStatus(ListingStatus.AVAILABLE);
        return listing;
    }
}
