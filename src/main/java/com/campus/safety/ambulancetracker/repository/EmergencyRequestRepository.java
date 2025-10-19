package com.campus.safety.ambulancetracker.repository;

import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {

    /**
     * Find all requests made by a specific User.
     */
    List<EmergencyRequest> findByUser(User user);

    /**
     * Find all requests currently assigned to a specific Ambulance.
     */
    List<EmergencyRequest> findByAmbulance(Ambulance ambulance);
    
    /**
     * Find all requests with a specific status (e.g., "PENDING").
     */
    List<EmergencyRequest> findByStatus(String status);
}
