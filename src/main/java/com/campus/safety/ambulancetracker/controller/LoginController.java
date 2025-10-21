package com.campus.safety.ambulancetracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {

        // Handle login error
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }

        // Handle logout message (use only this)
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        // Handle registration success
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login with your credentials.");
        }

        return "login"; // points to login.html
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
