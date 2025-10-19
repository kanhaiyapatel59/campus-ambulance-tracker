package com.campus.safety.ambulancetracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "ambulances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ambulanceId;

    @Column(nullable = false, unique = true, length = 20)
    private String vehicleNo;

    @Column(nullable = false, length = 100)
    private String driverName;

    @Column(nullable = false, length = 15)
    private String contactNo;

    // Status: e.g., "AVAILABLE", "EN_ROUTE", "ON_SCENE", "OUT_OF_SERVICE"
    @Column(nullable = false, length = 30)
    private String status; 

    // Current location description (e.g., "North Gate", "Admin Block")
    @Column(nullable = false, length = 100)
    private String location;

    // JPA Relationship: One ambulance can be assigned to many requests.
    @OneToMany(mappedBy = "ambulance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<EmergencyRequest> emergencyRequests;
}