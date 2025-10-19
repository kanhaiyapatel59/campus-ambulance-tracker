package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List; // ADD THIS IMPORT
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        return userRepository.save(user);
    }
    
    /**
     * Finds all users. (Needed for UserController)
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
}