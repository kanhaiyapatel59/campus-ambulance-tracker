package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus; // NEW IMPORT
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; // NEW IMPORT
import java.util.List;
import java.util.Optional;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    public List<Ambulance> findAll() {
        return ambulanceRepository.findAll();
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
        // ... implementation ...
        Optional<Ambulance> ambulanceOptional = ambulanceRepository.findById(ambulanceId);
        if (ambulanceOptional.isEmpty()) {
            throw new IllegalArgumentException("Ambulance not found with ID: " + ambulanceId);
        }
        
        Ambulance ambulance = ambulanceOptional.get();

        ambulance.setStatus(status);
        ambulance.setLatitude(latitude);
        ambulance.setLongitude(longitude);
        ambulance.setLastUpdated(LocalDateTime.now());
        
        return ambulanceRepository.save(ambulance);
    }

    public Optional<Ambulance> findById(Long ambulanceId) {
        return ambulanceRepository.findById(ambulanceId);
    }

    /**
     * Saves a new ambulance or updates an existing one. (Needed for management)
     */
    public Ambulance save(Ambulance ambulance) {
        return ambulanceRepository.save(ambulance);
    }
}