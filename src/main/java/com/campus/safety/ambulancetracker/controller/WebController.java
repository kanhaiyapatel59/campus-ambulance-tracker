package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.service.AmbulanceService;
import com.campus.safety.ambulancetracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private final AmbulanceService ambulanceService;
    private final UserService userService;

    public WebController(AmbulanceService ambulanceService, UserService userService) {
        this.ambulanceService = ambulanceService;
        this.userService = userService;
    }

    // Displays the main operational dashboard
    @GetMapping("/")
    public String dashboard(Model model) {
        // FIX: Call the public findAll() method on the service
        model.addAttribute("ambulances", ambulanceService.findAll()); 
        return "dashboard"; // Renders src/main/resources/templates/dashboard.html
    }

    // Displays the user registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user-register"; // Renders src/main/resources/templates/user-register.html
    }

    // Handles the form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/"; // Redirects back to the dashboard after successful registration
    }
}