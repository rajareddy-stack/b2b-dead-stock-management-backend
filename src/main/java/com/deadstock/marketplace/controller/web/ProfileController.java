package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.model.BusinessProfile;
import com.deadstock.marketplace.model.Notification;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.NotificationService;
import com.deadstock.marketplace.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final NotificationService notificationService;

    public ProfileController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User user = userService.getCurrentUser();
        BusinessProfile profile = user.getBusinessProfile();
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "profile/view";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String companyName,
                              @RequestParam String taxId,
                              @RequestParam String address,
                              @RequestParam String city,
                              @RequestParam String state,
                              @RequestParam String postalCode,
                              @RequestParam String phone,
                              @RequestParam String industryType,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        try {
            userService.updateBusinessProfile(user.getId(), companyName, taxId, address, city, state, postalCode, phone, industryType);
            redirectAttributes.addFlashAttribute("successMessage", "Business profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile/notifications")
    public String viewNotifications(Model model) {
        User user = userService.getCurrentUser();
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        model.addAttribute("notifications", notifications);
        return "profile/notifications";
    }

    @PostMapping("/profile/notifications/read/{id}")
    public String markNotificationRead(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        notificationService.markAsRead(id, user);
        return "redirect:/profile/notifications";
    }

    @PostMapping("/profile/notifications/read-all")
    public String markAllRead() {
        User user = userService.getCurrentUser();
        notificationService.markAllAsRead(user);
        return "redirect:/profile/notifications";
    }
}
