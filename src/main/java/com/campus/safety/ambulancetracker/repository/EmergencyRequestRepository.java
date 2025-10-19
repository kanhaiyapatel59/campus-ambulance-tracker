package com.campus.safety.ambulancetracker.repository;

import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    
    // Find requests by status
    List<EmergencyRequest> findByStatus(String status);
    
    // Find requests assigned to a specific ambulance ID
    List<EmergencyRequest> findByAmbulanceId(Long ambulanceId);
}