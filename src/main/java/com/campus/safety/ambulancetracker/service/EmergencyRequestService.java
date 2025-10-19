package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;
import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import com.campus.safety.ambulancetracker.repository.EmergencyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmergencyRequestService {

    private final EmergencyRequestRepository requestRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final UserService userService;
    private final AmbulanceService ambulanceService;

    // Define coordinates for the home base (placeholder for now)
    private static final Double BASE_LATITUDE = 12.9716;
    private static final Double BASE_LONGITUDE = 77.5946;

    // Constant for average response time calculation
    private static final String DEFAULT_DURATION = "0 min 0 sec";

    // Inject dependencies
    public EmergencyRequestService(EmergencyRequestRepository requestRepository,
                                   UserService userService,
                                   AmbulanceService ambulanceService,
                                   AmbulanceRepository ambulanceRepository) {
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.ambulanceService = ambulanceService;
        this.ambulanceRepository = ambulanceRepository;
    }

    /**
     * Creates a new emergency request and auto-assigns an ambulance if available.
     */
    @Transactional
    public EmergencyRequest createAndAssignRequest(Long userId, String patientDetails, String destination) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Ambulance> availableAmbulances = ambulanceService.findAvailableAmbulances();
        if (availableAmbulances.isEmpty()) {
            return saveNewRequest(user, null, "PENDING", patientDetails, destination);
        }

        Ambulance assignedAmbulance = availableAmbulances.get(0);

        assignedAmbulance = ambulanceService.updateStatusAndLocation(
                assignedAmbulance.getId(),
                AmbulanceStatus.EN_ROUTE,
                assignedAmbulance.getLatitude(),
                assignedAmbulance.getLongitude()
        );

        EmergencyRequest newRequest = saveNewRequest(user, assignedAmbulance, "ASSIGNED", patientDetails, destination);
        newRequest.setStartTime(LocalDateTime.now());
        return requestRepository.save(newRequest);
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
     * Marks a request as completed and reassigns the ambulance if pending requests exist.
     */
    @Transactional
    public EmergencyRequest completeRequest(Long requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with ID: " + requestId));

        Ambulance completedAmbulance = request.getAmbulance();

        if (!"COMPLETED".equals(request.getStatus()) && completedAmbulance != null) {
            request.setStatus("COMPLETED");
            request.setEndTime(LocalDateTime.now());

            ambulanceService.updateStatusAndLocation(
                    completedAmbulance.getId(),
                    AmbulanceStatus.AVAILABLE,
                    BASE_LATITUDE,
                    BASE_LONGITUDE
            );

            assignPendingRequest(completedAmbulance);
        }

        return requestRepository.save(request);
    }

    /**
     * Finds emergency requests by status.
     */
    public List<EmergencyRequest> findRequestsByStatus(String status) {
        return requestRepository.findByStatus(status);
    }

    /**
     * Finds the oldest PENDING request and assigns it to a newly available ambulance.
     */
    private void assignPendingRequest(Ambulance availableAmbulance) {
        Optional<EmergencyRequest> pendingRequestOpt =
                requestRepository.findTopByStatusOrderByRequestTimeAsc("PENDING");

        if (pendingRequestOpt.isPresent()) {
            EmergencyRequest pendingRequest = pendingRequestOpt.get();

            pendingRequest.setAmbulance(availableAmbulance);
            pendingRequest.setStatus("ASSIGNED");
            pendingRequest.setStartTime(LocalDateTime.now());
            requestRepository.save(pendingRequest);

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

    /**
     * Generates a map of key performance indicators (KPIs) for the dispatch system.
     */
    @Transactional(readOnly = true)
    public Map<String, String> generateReports() {
        Map<String, String> reportData = new HashMap<>();

        long totalRequests = requestRepository.count();
        long completedRequests = requestRepository.countByStatus("COMPLETED");
        long pendingRequests = requestRepository.countByStatus("PENDING");
        long assignedRequests = requestRepository.countByStatus("ASSIGNED");

        reportData.put("totalRequests", String.valueOf(totalRequests));
        reportData.put("completedRequests", String.valueOf(completedRequests));
        reportData.put("pendingRequests", String.valueOf(pendingRequests));
        reportData.put("assignedRequests", String.valueOf(assignedRequests));

        reportData.put("averageDuration", calculateAverageDuration(completedRequests));
        reportData.put("busiestAmbulance", findBusiestAmbulance());

        return reportData;
    }

    /**
     * Helper method to calculate average completion time for all completed requests.
     */
    private String calculateAverageDuration(long completedRequests) {
        if (completedRequests == 0) {
            return DEFAULT_DURATION;
        }

        List<EmergencyRequest> completed = requestRepository
                .findByStatusAndStartTimeIsNotNullAndEndTimeIsNotNull("COMPLETED");

        long totalSeconds = 0;
        for (EmergencyRequest request : completed) {
            Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
            totalSeconds += duration.getSeconds();
        }

        long averageSeconds = totalSeconds / completedRequests;
        long minutes = averageSeconds / 60;
        long seconds = averageSeconds % 60;

        return String.format("%d min %d sec", minutes, seconds);
    }

    /**
     * ✅ Updated Helper method using JPQL to find the busiest ambulance (by completed request count).
     */
    private String findBusiestAmbulance() {
        // The JPQL query now returns a list of [ambulance_id, request_count]
        List<Object[]> result = requestRepository.findBusiestAmbulanceIdJPQL();

        if (result.isEmpty()) {
            return "N/A";
        }

        // First element is the busiest
        Object[] busiestData = result.get(0);

        Long ambulanceId = (Long) busiestData[0];
        Long requestCount = ((Number) busiestData[1]).longValue();

        Optional<Ambulance> ambulanceOpt = ambulanceRepository.findById(ambulanceId);

        if (ambulanceOpt.isPresent()) {
            Ambulance ambulance = ambulanceOpt.get();
            return String.format("%s (%d requests)", ambulance.getVehicleNo(), requestCount);
        } else {
            return String.format("Ambulance ID %d (%d requests)", ambulanceId, requestCount);
        }
    }
}
