package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.EmergencyRequestRepository;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository requestRepository;
    private final UserService userService;
    private final AmbulanceService ambulanceService;

    // Inject all required components
    public EmergencyRequestService(EmergencyRequestRepository requestRepository, 
                                   UserService userService, 
                                   AmbulanceService ambulanceService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.ambulanceService = ambulanceService;
    }

    /**
     * 1. Creates a new emergency request.
     * 2. Automatically assigns the first available ambulance.
     * This method is transactional to ensure both the request is saved and the ambulance status is updated.
     */
    @Transactional
    public EmergencyRequest createAndAssignRequest(Long userId, String patientDetails, String destination) {
        // 1. Validate and fetch the requesting user
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 2. Find an available ambulance
        List<Ambulance> availableAmbulances = ambulanceService.findAvailableAmbulances();
        if (availableAmbulances.isEmpty()) {
            // Create request as PENDING if no ambulance is available
            return saveNewRequest(user, null, "PENDING", patientDetails, destination);
        }

        // 3. Assign the first available ambulance
        Ambulance assignedAmbulance = availableAmbulances.get(0);
        
        // 4. Update the ambulance status
        assignedAmbulance = ambulanceService.updateStatusAndLocation(
            assignedAmbulance.getAmbulanceId(), 
            "EN_ROUTE", 
            assignedAmbulance.getLocation()
        );

        // 5. Save the request with ASSIGNED status
        EmergencyRequest newRequest = saveNewRequest(user, assignedAmbulance, "ASSIGNED", patientDetails, destination);
        newRequest.setStartTime(LocalDateTime.now()); // Mark start time upon assignment

        return newRequest;
    }

    private EmergencyRequest saveNewRequest(User user, Ambulance ambulance, String status, String patientDetails, String destination) {
        EmergencyRequest request = new EmergencyRequest();
        request.setUser(user);
        request.setAmbulance(ambulance);
        request.setRequestTime(LocalDateTime.now());
        request.setStatus(status);
        request.setPatientDetails(patientDetails);
        request.setDestination(destination);
        return requestRepository.save(request);
    }
    
    /**
     * Updates an existing request status to COMPLETED.
     */
    @Transactional
    public EmergencyRequest completeRequest(Long requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with ID: " + requestId));

        if (!request.getStatus().equals("COMPLETED") && request.getAmbulance() != null) {
            // 1. Mark the request as completed
            request.setStatus("COMPLETED");
            request.setEndTime(LocalDateTime.now());
            
            // 2. Mark the assigned ambulance as available again
            ambulanceService.updateStatusAndLocation(
                request.getAmbulance().getAmbulanceId(), 
                "AVAILABLE", 
                "Campus Base" // Assume ambulance returns to base
            );
        }
        
        return requestRepository.save(request);
    }
}
