package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint: POST /api/users
     * Registers a new user (used by the admin or registration form).
     */
    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Assuming you add a save() method in your UserService
        User savedUser = userService.save(user); 
        // Returns 201 Created
        return ResponseEntity.status(201).body(savedUser);
    }
    
    // Endpoint: GET /api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
}