package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus; // NEW IMPORT
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

    // Define coordinates for the home base (placeholder for now)
    private static final Double BASE_LATITUDE = 12.9716; 
    private static final Double BASE_LONGITUDE = 77.5946;

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
     */
    @Transactional
    public EmergencyRequest createAndAssignRequest(Long userId, String patientDetails, String destination) {
        // 1. Validate and fetch the requesting user
        // Note: Assumes userService.findById returns an Optional<User>
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
        // FIX: Changed parameter to AmbulanceStatus.EN_ROUTE and passed existing coordinates
        assignedAmbulance = ambulanceService.updateStatusAndLocation(
            assignedAmbulance.getId(), // FIX: Used getId() instead of getAmbulanceId()
            AmbulanceStatus.EN_ROUTE, 
            assignedAmbulance.getLatitude(), 
            assignedAmbulance.getLongitude()
        );

        // 5. Save the request with ASSIGNED status
        EmergencyRequest newRequest = saveNewRequest(user, assignedAmbulance, "ASSIGNED", patientDetails, destination);
        // Note: Assuming setStartTime is a method you will define on EmergencyRequest
        // newRequest.setStartTime(LocalDateTime.now()); 

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
     * Updates an existing request status to COMPLETED and frees the ambulance.
     */
    @Transactional
    public EmergencyRequest completeRequest(Long requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with ID: " + requestId));

        if (!request.getStatus().equals("COMPLETED") && request.getAmbulance() != null) {
            // 1. Mark the request as completed
            request.setStatus("COMPLETED");
            // Note: Assuming setEndTime is a method you will define on EmergencyRequest
            // request.setEndTime(LocalDateTime.now()); 
            
            // 2. Mark the assigned ambulance as available again at the base location
            // FIX: Changed parameter to AmbulanceStatus.AVAILABLE and passed BASE coordinates
            ambulanceService.updateStatusAndLocation(
                request.getAmbulance().getId(), // FIX: Used getId() instead of getAmbulanceId()
                AmbulanceStatus.AVAILABLE, 
                BASE_LATITUDE, 
                BASE_LONGITUDE
            );
        }
        
        return requestRepository.save(request);
    }
}