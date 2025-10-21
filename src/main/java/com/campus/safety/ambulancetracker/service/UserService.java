package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Finds a user by ID. Used by EmergencyRequestService.
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Saves a new user or updates an existing one. (Needed for UserController)
     */
    public User save(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    /**
     * Finds all users. (Needed for UserController)
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Find user by username (for login)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}