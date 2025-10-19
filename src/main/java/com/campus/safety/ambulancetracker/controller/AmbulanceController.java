package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;
import com.campus.safety.ambulancetracker.service.AmbulanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    public AmbulanceController(AmbulanceService ambulanceService) {
        this.ambulanceService = ambulanceService;
    }

    // Endpoint: GET /api/ambulances
    @GetMapping
    public List<Ambulance> getAllAmbulances() {
        // FIX: Call the public findAll() method on the service
        return ambulanceService.findAll();
    }

    // Endpoint: GET /api/ambulances/available
    @GetMapping("/available")
    public List<Ambulance> getAvailableAmbulances() {
        return ambulanceService.findAvailableAmbulances();
    }
    
    // Endpoint: PUT /api/ambulances/{id}/status
    // Used to simulate an ambulance reporting a status change or location update
    @PutMapping("/{id}/status")
    public ResponseEntity<Ambulance> updateAmbulanceStatusAndLocation(
            @PathVariable Long id,
            @RequestParam AmbulanceStatus status,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        
        try {
            Ambulance updatedAmbulance = ambulanceService.updateStatusAndLocation(id, status, lat, lng);
            return ResponseEntity.ok(updatedAmbulance);
        } catch (IllegalArgumentException e) {
            // Returns 404 Not Found if the ID is invalid
            return ResponseEntity.notFound().build();
        }
    }
}