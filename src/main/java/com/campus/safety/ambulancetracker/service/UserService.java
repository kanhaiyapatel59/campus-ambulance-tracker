package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service // Marks this class as a Spring service component
public class UserService {

    private final UserRepository userRepository;

    // Dependency Injection: Spring automatically injects the repository implementation
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user (student/staff/admin).
     */
    public User registerUser(User user) {
        // Simple validation: check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }
        
        // Set join date automatically
        user.setJoinDate(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}