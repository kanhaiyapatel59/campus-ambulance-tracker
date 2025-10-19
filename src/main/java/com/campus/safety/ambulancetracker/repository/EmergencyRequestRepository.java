package com.campus.safety.ambulancetracker.repository;

import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    
    // Find requests by status
    List<EmergencyRequest> findByStatus(String status);
    
    // Find requests assigned to a specific ambulance ID
    List<EmergencyRequest> findByAmbulanceId(Long ambulanceId);

    // Finds the oldest PENDING request (based on requestTime)
    Optional<EmergencyRequest> findTopByStatusOrderByRequestTimeAsc(String status);
    
    // Count requests by status
    long countByStatus(String status);

    // Find all COMPLETED requests with valid start and end times (for duration calculation)
    List<EmergencyRequest> findByStatusAndStartTimeIsNotNullAndEndTimeIsNotNull(String status);
    
    // ✅ Native Query: Find the ambulance ID with the highest number of completed requests
    @Query(value = "SELECT r.ambulance_id, COUNT(r.id) AS request_count " +
                   "FROM emergency_request r " +
                   "WHERE r.status = 'COMPLETED' AND r.ambulance_id IS NOT NULL " +
                   "GROUP BY r.ambulance_id " +
                   "ORDER BY request_count DESC " +
                   "LIMIT 1",
           nativeQuery = true)
    List<Object[]> findBusiestAmbulanceIdNative();

    // ✅ JPQL Query (portable alternative to native query)
    // JPQL does not support LIMIT, so we fetch all and handle top-1 in Java.
    @Query("SELECT r.ambulance.id, COUNT(r) " +
           "FROM EmergencyRequest r " +
           "WHERE r.status = 'COMPLETED' AND r.ambulance IS NOT NULL " +
           "GROUP BY r.ambulance.id " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findBusiestAmbulanceIdJPQL();
}
