package com.campus.safety.ambulancetracker.repository;

import com.campus.safety.ambulancetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository requires the Entity Type (User) and the type of its Primary Key (Long)
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Custom method defined by Spring Data JPA query conventions.
     * Finds a User entity by its email field.
     */
    Optional<User> findByEmail(String email);
}