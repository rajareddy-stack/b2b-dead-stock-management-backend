package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.dto.OfferForm;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.AdminService;
import com.deadstock.marketplace.service.CategoryService;
import com.deadstock.marketplace.service.ListingService;
import com.deadstock.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final ListingService listingService;
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final UserService userService;

    public HomeController(ListingService listingService,
                          CategoryService categoryService,
                          AdminService adminService,
                          UserService userService) {
        this.listingService = listingService;
        this.categoryService = categoryService;
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Listing> featuredListings = listingService.getAllActiveListings();
        if (featuredListings.size() > 6) {
            featuredListings = featuredListings.subList(0, 6);
        }
        model.addAttribute("featuredListings", featuredListings);
        model.addAttribute("stats", adminService.getAdminStats());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "home";
    }

    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) String location,
                         @RequestParam(required = false) String query,
                         Model model) {
        List<Listing> listings = listingService.searchListings(categoryId, location, query);
        model.addAttribute("listings", listings);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("locations", listingService.getDistinctLocations());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("searchQuery", query);
        return "listings/browse";
    }

    @GetMapping("/listings/{id}")
    public String viewListing(@PathVariable Long id, Model model) {
        Listing listing = listingService.getListingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found with ID: " + id));

        model.addAttribute("listing", listing);
        User currentUser = userService.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        OfferForm offerForm = new OfferForm();
        offerForm.setListingId(listing.getId());
        offerForm.setQuantity(listing.getQuantity());
        offerForm.setOfferPrice(listing.getPrice());
        model.addAttribute("offerForm", offerForm);

        return "listings/detail";
    }
}
