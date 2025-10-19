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
    private Long requestId;

    // Relationship: Many requests belong to one User (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Defines the Foreign Key column name
    private User user;

    // Relationship: Many requests can be handled by one Ambulance (Many-to-One)
    // Ambulance can be null if the request hasn't been assigned yet.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id")
    private Ambulance ambulance;

    @Column(nullable = false)
    private LocalDateTime requestTime;

    private LocalDateTime startTime; // When the ambulance begins the trip
    
    private LocalDateTime endTime; // When the request is closed/completed

    // Status: e.g., "PENDING", "ASSIGNED", "EN_ROUTE", "COMPLETED", "CANCELLED"
    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private String patientDetails; // Brief patient description and complaint

    @Column(nullable = false)
    private String destination; // Where the patient needs to be taken (e.g., Hospital X)
}