package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.dto.AdminStatsDto;
import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Listing;
import com.deadstock.marketplace.service.AdminService;
import com.deadstock.marketplace.service.ListingService;
import com.deadstock.marketplace.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ListingService listingService;
    private final UserService userService;

    public AdminController(AdminService adminService,
                           ListingService listingService,
                           UserService userService) {
        this.adminService = adminService;
        this.listingService = listingService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AdminStatsDto stats = adminService.getAdminStats();
        List<BusinessProfile> businesses = adminService.getAllBusinesses();
        List<Listing> listings = listingService.getAllListings();

        model.addAttribute("stats", stats);
        model.addAttribute("businesses", businesses);
        model.addAttribute("listings", listings);
        return "admin/dashboard";
    }

    @PostMapping("/businesses/verify/{id}")
    public String verifyBusiness(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.verifyBusiness(id);
            redirectAttributes.addFlashAttribute("successMessage", "Business has been verified and approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/businesses/reject/{id}")
    public String rejectBusiness(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectBusiness(id);
            redirectAttributes.addFlashAttribute("infoMessage", "Business verification has been rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/listings/delete/{id}")
    public String moderateDeleteListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            listingService.deleteListing(id, userService.getCurrentUser(), true);
            redirectAttributes.addFlashAttribute("successMessage", "Listing removed by admin moderation.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
