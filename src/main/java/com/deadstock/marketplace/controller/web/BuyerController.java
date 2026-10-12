package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.dto.OfferForm;
import com.deadstock.marketplace.model.Offer;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.model.enums.OfferStatus;
import com.deadstock.marketplace.service.OfferService;
import com.deadstock.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/buyer")
@PreAuthorize("hasRole('BUYER')")
public class BuyerController {

    private final OfferService offerService;
    private final UserService userService;

    public BuyerController(OfferService offerService, UserService userService) {
        this.offerService = offerService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User buyer = userService.getCurrentUser();
        List<Offer> myOffers = offerService.getOffersByBuyer(buyer);
        model.addAttribute("myOffers", myOffers);
        model.addAttribute("acceptedOfferCount", myOffers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.ACCEPTED)
                .count());
        return "buyer/dashboard";
    }

    @PostMapping("/offers/submit")
    public String submitOffer(@Valid @ModelAttribute("offerForm") OfferForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid offer parameters. Please check values.");
            return "redirect:/listings/" + form.getListingId();
        }

        User buyer = userService.getCurrentUser();
        try {
            offerService.createOffer(form, buyer);
            redirectAttributes.addFlashAttribute("successMessage", "Your offer has been sent to the seller!");
            return "redirect:/buyer/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/" + form.getListingId();
        }
    }

    @PostMapping("/offers/accept/{id}")
    public String acceptCounterOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User buyer = userService.getCurrentUser();
        try {
            offerService.acceptCounterOfferByBuyer(id, buyer);
            redirectAttributes.addFlashAttribute("successMessage", "Counter-offer accepted! Deal Confirmed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/dashboard";
    }

    @PostMapping("/offers/reject/{id}")
    public String rejectOffer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User buyer = userService.getCurrentUser();
        try {
            offerService.rejectOffer(id, buyer);
            redirectAttributes.addFlashAttribute("infoMessage", "Deal declined.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/dashboard";
    }
}
