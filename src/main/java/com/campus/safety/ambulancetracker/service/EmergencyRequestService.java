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
import java.util.Optional;

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

        Ambulance completedAmbulance = request.getAmbulance();

        if (!request.getStatus().equals("COMPLETED") && completedAmbulance != null) {
            // 1. Mark the request as completed
            request.setStatus("COMPLETED");
            request.setEndTime(LocalDateTime.now());
            
            // 2. Mark the assigned ambulance as available again at the base location
            ambulanceService.updateStatusAndLocation(
                completedAmbulance.getId(), 
                AmbulanceStatus.AVAILABLE, 
                BASE_LATITUDE, 
                BASE_LONGITUDE
            );
            
            // 3. CRUCIAL NEW STEP: Immediately check for and assign the oldest PENDING request
            assignPendingRequest(completedAmbulance);
        }
        
        return requestRepository.save(request);
    }

    /**
     * Finds emergency requests based on their status (e.g., PENDING, ASSIGNED).
     */
    public List<EmergencyRequest> findRequestsByStatus(String status) {
        return requestRepository.findByStatus(status);
    }

    /**
     * Finds the oldest PENDING request and assigns the newly available ambulance to it.
     * This method is called after an ambulance returns to AVAILABLE status.
     * @param availableAmbulance The ambulance that just returned to base.
     */
    private void assignPendingRequest(Ambulance availableAmbulance) {
        // Find the oldest PENDING request (FIFO - First In, First Out)
        Optional<EmergencyRequest> pendingRequestOpt = 
            requestRepository.findTopByStatusOrderByRequestTimeAsc("PENDING");

        if (pendingRequestOpt.isPresent()) {
            EmergencyRequest pendingRequest = pendingRequestOpt.get();
            
            // 1. Update the PENDING request with the newly available ambulance
            pendingRequest.setAmbulance(availableAmbulance);
            pendingRequest.setStatus("ASSIGNED");
            pendingRequest.setStartTime(LocalDateTime.now()); 
            requestRepository.save(pendingRequest);

            // 2. Update the now-assigned ambulance's status to EN_ROUTE
            // The location will be the base location set just moments ago in completeRequest.
            ambulanceService.updateStatusAndLocation(
                availableAmbulance.getId(), 
                AmbulanceStatus.EN_ROUTE, 
                availableAmbulance.getLatitude(), 
                availableAmbulance.getLongitude()
            );

            System.out.println(">>> PENDING request ID " + pendingRequest.getId() + 
                               " auto-assigned to ambulance " + availableAmbulance.getVehicleNo() + " <<<");
        }
    }
}