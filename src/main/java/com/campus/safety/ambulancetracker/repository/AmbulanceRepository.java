package com.campus.safety.ambulancetracker.repository;

import com.campus.safety.ambulancetracker.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    /**
     * Custom method to find all Ambulances by their status (e.g., "AVAILABLE").
     */
    List<Ambulance> findByStatus(String status);
}