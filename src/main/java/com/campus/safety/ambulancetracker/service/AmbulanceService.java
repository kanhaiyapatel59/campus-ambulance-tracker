package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus; // NEW IMPORT
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; // NEW IMPORT
import java.util.List;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    /**
     * Finds all ambulances currently marked as AVAILABLE.
     */
    public List<Ambulance> findAvailableAmbulances() {
        // FIX: Use the AmbulanceStatus Enum instead of a String
        return ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE);
    }

    /**
     * Updates an ambulance's status and location.
     * FIX: Updated method signature to use AmbulanceStatus and geo-coordinates.
     */
    @Transactional
    public Ambulance updateStatusAndLocation(Long ambulanceId, AmbulanceStatus status, Double latitude, Double longitude) {
        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                // FIX: Primary key is now 'id'
                .orElseThrow(() -> new IllegalArgumentException("Ambulance not found with ID: " + ambulanceId));

        // Status validation is implicit due to the AmbulanceStatus type, no need for manual List check.

        ambulance.setStatus(status);
        
        // FIX: Set latitude and longitude instead of location string
        ambulance.setLatitude(latitude);
        ambulance.setLongitude(longitude);
        ambulance.setLastUpdated(LocalDateTime.now()); // Record the update time
        
        return ambulanceRepository.save(ambulance);
    }
}