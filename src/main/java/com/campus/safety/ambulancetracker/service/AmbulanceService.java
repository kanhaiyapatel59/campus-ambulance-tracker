package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    /**
     * Finds all ambulances currently marked as "AVAILABLE".
     */
    public List<Ambulance> findAvailableAmbulances() {
        return ambulanceRepository.findByStatus("AVAILABLE");
    }

    /**
     * Updates an ambulance's status and location.
     */
    public Ambulance updateStatusAndLocation(Long ambulanceId, String status, String location) {
        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new IllegalArgumentException("Ambulance not found with ID: " + ambulanceId));

        // Basic status validation
        if (!List.of("AVAILABLE", "EN_ROUTE", "ON_SCENE", "OUT_OF_SERVICE").contains(status)) {
            throw new IllegalArgumentException("Invalid status provided: " + status);
        }

        ambulance.setStatus(status);
        ambulance.setLocation(location);
        
        return ambulanceRepository.save(ambulance);
    }
}