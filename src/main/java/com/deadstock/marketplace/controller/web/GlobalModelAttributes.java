package com.deadstock.marketplace.controller.web;

import com.deadstock.marketplace.model.Category;
import com.deadstock.marketplace.model.User;
import com.deadstock.marketplace.service.CategoryService;
import com.deadstock.marketplace.service.NotificationService;
import com.deadstock.marketplace.service.UserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.deadstock.marketplace.controller.web")
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationService notificationService;
    private final CategoryService categoryService;

    public GlobalModelAttributes(UserService userService,
                                 NotificationService notificationService,
                                 CategoryService categoryService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        User user = userService.getCurrentUser();
        return notificationService.getUnreadCount(user);
    }

    @ModelAttribute("globalCategories")
    public List<Category> getGlobalCategories() {
        return categoryService.getAllCategories();
    }
}
