package com.campus.safety.ambulancetracker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity // Marks this class as a JPA entity, mapped to a database table
@Table(name = "users") // Explicitly names the database table
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok: Generates a constructor with no arguments (required by JPA)
@AllArgsConstructor // Lombok: Generates a constructor with all arguments
public class User {

    @Id // Designates this field as the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID to be auto-incremented by MySQL
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100) // Constraint: Email must be unique
    private String email;

    @Column(nullable = false, length = 15)
    private String contactNo;

    @Column(nullable = false, length = 20)
    private String role; // e.g., "STUDENT", "STAFF", "ADMIN"

    @Column(nullable = false)
    private LocalDateTime joinDate;

    // JPA Relationship: One user can make many emergency requests (One-to-Many).
    // The 'mappedBy' attribute indicates the field in the 'EmergencyRequest' entity 
    // that owns the relationship (the foreign key).
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<EmergencyRequest> emergencyRequests;
}