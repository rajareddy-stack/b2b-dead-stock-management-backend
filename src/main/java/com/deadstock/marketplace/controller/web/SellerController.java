package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.dto.CounterOfferForm;
import com.deadstock.marketplace.dto.ListingForm;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.ListingStatus;
import com.deadstock.marketplace.model.enums.OfferStatus;
import com.deadstock.marketplace.service.CategoryService;
import com.deadstock.marketplace.service.ListingService;
import com.deadstock.marketplace.service.OfferService;
import com.deadstock.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final ListingService listingService;
    private final OfferService offerService;
    private final UserService userService;
    private final CategoryService categoryService;

    public SellerController(ListingService listingService,
                            OfferService offerService,
                            UserService userService,
                            CategoryService categoryService) {
        this.listingService = listingService;
        this.offerService = offerService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User seller = userService.getCurrentUser();
        List<Listing> myListings = listingService.getListingsBySeller(seller);
        List<Offer> incomingOffers = offerService.getIncomingOffersForSeller(seller);
        List<Listing> activeListings = myListings.stream()
                .filter(this::isActiveListing)
                .toList();
        BigDecimal listedStockValue = activeListings.stream()
                .map(listing -> listing.getPrice().multiply(BigDecimal.valueOf(listing.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tiedUpCapital = activeListings.stream()
                .map(listing -> listing.getOriginalPrice().multiply(BigDecimal.valueOf(listing.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AgeBucket> ageBuckets = List.of(
                createAgeBucket("Under 30 days", activeListings.stream()
                        .filter(listing -> daysListed(listing) < 30)
                        .toList(), "fresh"),
                createAgeBucket("30–59 days", activeListings.stream()
                        .filter(listing -> isWithinAgeRange(listing, 30, 60))
                        .toList(), "watch"),
                createAgeBucket("60–89 days", activeListings.stream()
                        .filter(listing -> isWithinAgeRange(listing, 60, 90))
                        .toList(), "risk"),
                createAgeBucket("90+ days", activeListings.stream()
                        .filter(listing -> daysListed(listing) >= 90)
                        .toList(), "critical")
        );
        Map<String, BigDecimal> categoryValues = activeListings.stream()
                .collect(Collectors.groupingBy(
                        listing -> listing.getCategory().getTitle(),
                        Collectors.mapping(
                                listing -> listing.getOriginalPrice().multiply(BigDecimal.valueOf(listing.getQuantity())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        List<CategoryExposure> topCategories = categoryValues.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(4)
                .map(entry -> new CategoryExposure(entry.getKey(), entry.getValue(),
                        activeListings.stream().filter(listing -> listing.getCategory().getTitle().equals(entry.getKey())).count()))
                .toList();
        List<String> listingCategories = activeListings.stream()
                .map(listing -> listing.getCategory().getTitle())
                .distinct()
                .sorted()
                .toList();
        List<String> listingLocations = activeListings.stream()
                .map(Listing::getLocation)
                .distinct()
                .sorted()
                .toList();
        long pendingOfferCount = incomingOffers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING
                        || offer.getStatus() == OfferStatus.COUNTER_OFFERED)
                .count();

        model.addAttribute("myListings", myListings);
        model.addAttribute("activeListings", activeListings);
        model.addAttribute("incomingOffers", incomingOffers);
        model.addAttribute("listedStockValue", listedStockValue);
        model.addAttribute("tiedUpCapital", tiedUpCapital);
        model.addAttribute("activeListingCount", activeListings.size());
        model.addAttribute("ageBuckets", ageBuckets);
        model.addAttribute("topCategories", topCategories);
        model.addAttribute("listingCategories", listingCategories);
        model.addAttribute("listingLocations", listingLocations);
        model.addAttribute("oldestActiveListing", activeListings.stream()
                .min(Comparator.comparing(Listing::getCreatedAt))
                .orElse(null));
        model.addAttribute("pendingOfferCount", pendingOfferCount);
        model.addAttribute("counterOfferForm", new CounterOfferForm());
        return "seller/dashboard";
    }

    private boolean isActiveListing(Listing listing) {
        return listing.getStatus() == ListingStatus.AVAILABLE
                || listing.getStatus() == ListingStatus.UNDER_NEGOTIATION;
    }

    private long daysListed(Listing listing) {
        if (listing.getCreatedAt() == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(listing.getCreatedAt().toLocalDate(), LocalDate.now()));
    }

    private boolean isWithinAgeRange(Listing listing, long lowerInclusive, long upperExclusive) {
        long age = daysListed(listing);
        return age >= lowerInclusive && age < upperExclusive;
    }

    private AgeBucket createAgeBucket(String label, List<Listing> listings, String tone) {
        BigDecimal value = listings.stream()
                .map(listing -> listing.getOriginalPrice().multiply(BigDecimal.valueOf(listing.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AgeBucket(label, listings.size(), value, tone);
    }

    public record AgeBucket(String label, int listings, BigDecimal value, String tone) {
    }

    public record CategoryExposure(String name, BigDecimal value, long listings) {
    }

    @GetMapping("/listings/create")
    public String showCreateListingForm(Model model) {
        if (!model.containsAttribute("listingForm")) {
            ListingForm form = new ListingForm();
            User user = userService.getCurrentUser();
            if (user != null && user.getBusinessProfile() != null) {
                form.setLocation(user.getBusinessProfile().getCity() + ", " + user.getBusinessProfile().getState());
            }
            model.addAttribute("listingForm", form);
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "seller/create-listing";
    }

    @PostMapping("/listings/create")
    public String processCreateListing(@Valid @ModelAttribute("listingForm") ListingForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/create-listing";
        }

        User seller = userService.getCurrentUser();
        listingService.createListing(form, seller);
        redirectAttributes.addFlashAttribute("successMessage", "Dead stock listing created successfully!");
        return "redirect:/seller/dashboard";
    }

    @GetMapping("/listings/edit/{id}")
    public String showEditListingForm(@PathVariable Long id, Model model) {
        User seller = userService.getCurrentUser();
        Listing listing = listingService.getListingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        if (!listing.getSeller().getId().equals(seller.getId())) {
            return "redirect:/seller/dashboard?error=unauthorized";
        }

        ListingForm form = new ListingForm();
        form.setId(listing.getId());
        form.setTitle(listing.getTitle());
        form.setDescription(listing.getDescription());
        form.setCategoryId(listing.getCategory().getId());
        form.setOriginalPrice(listing.getOriginalPrice());
        form.setPrice(listing.getPrice());
        form.setQuantity(listing.getQuantity());
        form.setUnit(listing.getUnit());
        form.setCondition(listing.getCondition());
        form.setLocation(listing.getLocation());
        form.setImageUrl(listing.getImageUrl());

        model.addAttribute("listingForm", form);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "seller/create-listing";
    }

    @PostMapping("/listings/edit/{id}")
    public String processEditListing(@PathVariable Long id,
                                     @Valid @ModelAttribute("listingForm") ListingForm form,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "seller/create-listing";
        }

        User seller = userService.getCurrentUser();
        listingService.updateListing(id, form, seller);
        redirectAttributes.addFlashAttribute("successMessage", "Listing updated successfully!");
        return "redirect:/seller/dashboard";
    }

    @PostMapping("/listings/delete/{id}")
    public String deleteListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User seller = userService.getCurrentUser();
        listingService.deleteListing(id, seller, false);
        redirectAttributes.addFlashAttribute("successMessage", "Listing removed successfully.");
        return "redirect:/seller/dashboard";
    }

    @GetMapping("/offers")
    public String viewOffers(Model model) {
        User seller = userService.getCurrentUser();
        List<Offer> incomingOffers = offerService.getIncomingOffersForSeller(seller);
        model.addAttribute("incomingOffers", incomingOffers);
        model.addAttribute("counterOfferForm", new CounterOfferForm());
        return "seller/incoming-offers";
    }

    @PostMapping("/offers/counter/{id}")
    public String counterOffer(@PathVariable Long id,
                               @ModelAttribute("counterOfferForm") CounterOfferForm form,
                               RedirectAttributes redirectAttributes) {
        User seller = userService.getCurrentUser();
        try {
            offerService.sellerMakeCounterOffer(id, form, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Counter-offer sent to buyer successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/offers";
    }

    @PostMapping("/offers/accept/{id}")
    public String acceptOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User seller = userService.getCurrentUser();
        try {
            offerService.acceptOfferBySeller(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Offer accepted! Deal Confirmed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/offers";
    }

    @PostMapping("/offers/reject/{id}")
    public String rejectOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User seller = userService.getCurrentUser();
        try {
            offerService.rejectOffer(id, seller);
            redirectAttributes.addFlashAttribute("infoMessage", "Offer declined.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/offers";
    }
}
