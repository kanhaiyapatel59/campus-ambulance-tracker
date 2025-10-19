package com.campus.safety.ambulancetracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATIONSHIP: The user who initiated the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) 
    private User user;

    // RELATIONSHIP: Ambulance assigned to the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id") // Can be null if PENDING
    private Ambulance ambulance; 

    // DETAILS: Fields used by saveNewRequest
    @Column(name = "patient_details")
    private String patientDetails; 
    
    @Column(name = "destination")
    private String destination;

    // TIMESTAMPS: Fields used in the service logic
    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime; 

    @Column(name = "start_time")
    private LocalDateTime startTime; // When ambulance was assigned/dispatched

    @Column(name = "end_time")
    private LocalDateTime endTime; // When the request was completed

    @Column(name = "status", nullable = false, length = 30)
    private String status; // e.g., "PENDING", "ASSIGNED", "COMPLETED"

    @Column(name = "priority", length = 20)
    private String priority; // e.g., "HIGH", "MEDIUM"
}