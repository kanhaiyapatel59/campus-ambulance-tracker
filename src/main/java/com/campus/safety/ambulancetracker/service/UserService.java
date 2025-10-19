package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

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
}