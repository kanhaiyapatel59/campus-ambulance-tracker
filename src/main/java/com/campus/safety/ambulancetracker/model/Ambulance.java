package com.campus.safety.ambulancetracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Changed from ambulanceId to the standard 'id'

    @Column(name = "vehicle_no", nullable = false, unique = true, length = 20)
    private String vehicleNo;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "contact_no", nullable = false, length = 15)
    private String contactNo;

    // USE ENUM: Status will be mapped as a String in the database
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AmbulanceStatus status; 

    // ADDED: Precise location coordinates for tracking
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // ADDED: Timestamp to know when the location was last updated
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    // JPA Relationship: One ambulance can be assigned to many requests.
    @OneToMany(mappedBy = "ambulance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<EmergencyRequest> emergencyRequests;
}