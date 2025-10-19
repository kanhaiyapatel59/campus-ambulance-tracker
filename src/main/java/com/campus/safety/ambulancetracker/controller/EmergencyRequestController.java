package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.service.EmergencyRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class EmergencyRequestController {

    private final EmergencyRequestService requestService;

    public EmergencyRequestController(EmergencyRequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Endpoint: POST /api/requests
     * Creates a new emergency request and attempts to assign an available ambulance.
     */
    @PostMapping
    public ResponseEntity<EmergencyRequest> createEmergencyRequest(
            @RequestParam Long userId,
            @RequestParam String patientDetails,
            @RequestParam String destination) {

        try {
            EmergencyRequest newRequest = requestService.createAndAssignRequest(
                userId, 
                patientDetails, 
                destination
            );
            // Returns 201 Created
            return ResponseEntity.status(201).body(newRequest); 
        } catch (IllegalArgumentException e) {
            // Returns 400 Bad Request on user not found or other validation issues
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint: PUT /api/requests/{id}/complete
     * Marks an emergency request as completed and frees the assigned ambulance.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<EmergencyRequest> completeRequest(@PathVariable Long id) {
        try {
            EmergencyRequest completedRequest = requestService.completeRequest(id);
            return ResponseEntity.ok(completedRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}