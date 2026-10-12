package com.deadstock.marketplace.config;

import com.deadstock.marketplace.model.*;
import com.deadstock.marketplace.model.enums.*;
import com.deadstock.marketplace.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!production")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ListingRepository listingRepository;
    private final OfferRepository offerRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BusinessProfileRepository businessProfileRepository,
                           CategoryRepository categoryRepository,
                           ListingRepository listingRepository,
                           OfferRepository offerRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.categoryRepository = categoryRepository;
        this.listingRepository = listingRepository;
        this.offerRepository = offerRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return; // Data already seeded
        }

        // 1. Seed Categories
        Category catConstruction = categoryRepository.save(new Category(
                "Construction & Raw Metals",
                "Excess steel beams, rebar, aluminum sheets, timber, and hardware surplus.",
                "bi-hammer"
        ));

        Category catPackaging = categoryRepository.save(new Category(
                "Packaging & Plastics",
                "Overstocked corrugated boxes, stretch wrap rolls, pallets, and HDPE containers.",
                "bi-box-seam"
        ));

        Category catRestaurant = categoryRepository.save(new Category(
                "Restaurant & Food Service",
                "Unused commercial food equipment, bulk stainless steel furniture, and dry goods.",
                "bi-cup-hot"
        ));

        Category catMachinery = categoryRepository.save(new Category(
                "Industrial Machinery & Parts",
                "Dormant electric motors, CNC tooling, pumps, hydraulic valves, and electrical switchgear.",
                "bi-gear-wide-connected"
        ));

        Category catTextiles = categoryRepository.save(new Category(
                "Textiles & Raw Fabrics",
                "Surplus woven cotton, denim rolls, synthetic yarns, and apparel overruns.",
                "bi-scissors"
        ));

        Category catChemicals = categoryRepository.save(new Category(
                "Chemicals & Coatings",
                "Industrial paints, epoxy resins, solvents, lubricants, and surplus adhesives.",
                "bi-droplet-half"
        ));

        // 2. Seed Users & Business Profiles
        // Admin
        User adminUser = new User("admin@deadstock.com", passwordEncoder.encode("password123"), "System Administrator", Role.ADMIN);
        adminUser = userRepository.save(adminUser);
        businessProfileRepository.save(new BusinessProfile(
                adminUser, "StockSetu Marketplace Operations", "BUSINESS-ID-DEMO-001",
                "Business Park, Andheri East", "Mumbai", "Maharashtra", "400069",
                "+91 98765 00001", "B2B Platform Operations", VerificationStatus.APPROVED
        ));

        // Seller 1 (Verified)
        User seller1 = new User("seller@metrosteel.com", passwordEncoder.encode("password123"), "Marcus Vance", Role.SELLER);
        seller1 = userRepository.save(seller1);
        businessProfileRepository.save(new BusinessProfile(
                seller1, "Metro Steel & Alloy Fabricators", "BUSINESS-ID-DEMO-002",
                "Plot 18, GIDC Vatva", "Ahmedabad", "Gujarat", "382445",
                "+91 98765 11111", "Metals & Heavy Manufacturing", VerificationStatus.APPROVED
        ));

        // Buyer 1 (Verified)
        User buyer1 = new User("buyer@apexbuild.com", passwordEncoder.encode("password123"), "Sarah Chen", Role.BUYER);
        buyer1 = userRepository.save(buyer1);
        businessProfileRepository.save(new BusinessProfile(
                buyer1, "Apex Commercial Builders", "BUSINESS-ID-DEMO-003",
                "Baner Road, Baner", "Pune", "Maharashtra", "411045",
                "+91 98765 22222", "Commercial Construction & Contracting", VerificationStatus.APPROVED
        ));

        // Seller 2 (Pending Verification - for Admin review demo)
        User seller2 = new User("seller2@novapack.com", passwordEncoder.encode("password123"), "Elena Rostova", Role.SELLER);
        seller2 = userRepository.save(seller2);
        businessProfileRepository.save(new BusinessProfile(
                seller2, "Nova Industrial Packaging", "BUSINESS-ID-DEMO-004",
                "Plot 42, MIDC Industrial Area", "Nashik", "Maharashtra", "422010",
                "+91 98765 33333", "Logistics Packaging & Plastics", VerificationStatus.PENDING
        ));

        // 3. Seed Realistic Dead Stock Listings
        Listing listing1 = listingRepository.save(new Listing(
                seller1,
                catConstruction,
                "Surplus Structural Steel I-Beams (W12x26 - 30ft Lengths)",
                "Project surplus: 45 high-strength structural steel beams. Clean mill finish; mill test reports available on request. Stored indoors at the Ahmedabad warehouse.",
                BigDecimal.valueOf(62000.00),
                BigDecimal.valueOf(48000.00),
                45,
                "Tons",
                ItemCondition.BRAND_NEW_SURPLUS,
                "Vatva GIDC, Ahmedabad, Gujarat 382445",
                "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?auto=format&fit=crop&w=800&q=80"
        ));

        Listing listing2 = listingRepository.save(new Listing(
                seller1,
                catMachinery,
                "Overstocked Industrial 3-Phase Electric Induction Motors (15HP)",
                "Surplus order cancellation. 12 units of premium high-efficiency TEFC 3-phase induction motors (460V, 1750 RPM). Never installed, still secured on original wooden factory pallets.",
                BigDecimal.valueOf(29500.00),
                BigDecimal.valueOf(18200.00),
                12,
                "Units",
                ItemCondition.OVERSTOCKED,
                "Vatva GIDC, Ahmedabad, Gujarat 382445",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=800&q=80"
        ));

        Listing listing3 = listingRepository.save(new Listing(
                seller2,
                catPackaging,
                "Excess Heavy-Duty Double Wall Corrugated Boxes (24x18x18)",
                "Warehouse consolidation excess stock: 2,500 ECT-48 heavy-duty double wall brown corrugated cartons. Flat packed on 10 pallets. Perfect for industrial freight or e-commerce fulfillment.",
                BigDecimal.valueOf(155.00),
                BigDecimal.valueOf(92.00),
                2500,
                "Cartons",
                ItemCondition.BRAND_NEW_SURPLUS,
                "MIDC Industrial Area, Nashik, Maharashtra 422010",
                "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=800&q=80"
        ));

        Listing listing4 = listingRepository.save(new Listing(
                seller1,
                catRestaurant,
                "Commercial Grade Stainless Steel Worktables (60x30 with Undershelf)",
                "Unopened surplus from university cafeteria renovation project: 18 gauge 430 stainless steel work tables with adjustable galvanized lower shelf and bullet feet. Easy flat assembly.",
                BigDecimal.valueOf(25000.00),
                BigDecimal.valueOf(15000.00),
                18,
                "Tables",
                ItemCondition.OPEN_BOX,
                "Vatva GIDC, Ahmedabad, Gujarat 382445",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=800&q=80"
        ));

        Listing listing5 = listingRepository.save(new Listing(
                seller2,
                catTextiles,
                "Industrial Heavy Canvas Fabric Surplus Rolls (14oz Natural Cotton)",
                "End-of-contract roll stock: 35 rolls of 14oz unbleached natural cotton duck canvas. 60 inches wide, approx 100 yards per roll. Ideal for heavy tote bags, upholstery, and protective covers.",
                BigDecimal.valueOf(10500.00),
                BigDecimal.valueOf(6300.00),
                35,
                "Rolls",
                ItemCondition.BRAND_NEW_SURPLUS,
                "MIDC Industrial Area, Nashik, Maharashtra 422010",
                "https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?auto=format&fit=crop&w=800&q=80"
        ));

        // 4. Seed Negotiation Offer
        Offer offer1 = new Offer(
                listing1,
                buyer1,
                BigDecimal.valueOf(10500.00),
                20,
                "We can arrange two trucks for pickup in Ahmedabad this Friday. Would you consider ₹44,500 per ton for 20 tons?"
        );
        offer1.setStatus(OfferStatus.COUNTER_OFFERED);
        offer1.setCounterPrice(BigDecimal.valueOf(46000.00));
        offer1.setSellerMessage("We can release 20 tons at ₹46,000 per ton, subject to pickup by Friday.");
        offerRepository.save(offer1);

        listing1.setStatus(ListingStatus.UNDER_NEGOTIATION);
        listingRepository.save(listing1);

        // 5. Seed Notifications
        notificationRepository.save(new Notification(
                seller1,
                "New Counter-Offer Sent",
                "A counter-offer of ₹46,000 per ton was sent to Apex Commercial Builders for Structural Steel I-Beams.",
                "/seller/offers"
        ));

        notificationRepository.save(new Notification(
                buyer1,
                "Counter-Offer Received",
                "Metro Steel countered your offer at ₹46,000 per ton for 20 tons of Structural Steel I-Beams.",
                "/buyer/dashboard"
        ));
    }
}
