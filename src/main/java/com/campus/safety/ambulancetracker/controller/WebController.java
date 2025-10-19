package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.service.AmbulanceService;
import com.campus.safety.ambulancetracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.service.EmergencyRequestService;

import java.time.LocalDateTime;
import java.util.List;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;

@Controller
public class WebController {

    private final EmergencyRequestService requestService; 
    private final AmbulanceService ambulanceService;
    private final UserService userService;

    public WebController(AmbulanceService ambulanceService, UserService userService, EmergencyRequestService requestService) {
        this.ambulanceService = ambulanceService;
        this.userService = userService;
        this.requestService = requestService;
    }

    // Displays the main operational dashboard
    @GetMapping("/")
    public String dashboard(Model model) {
        // FIX: Call the public findAll() method on the service
        model.addAttribute("ambulances", ambulanceService.findAll()); 
        return "dashboard"; // Renders src/main/resources/templates/dashboard.html
    }

    // Displays the user registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user-register"; // Renders src/main/resources/templates/user-register.html
    }

    // Handles the form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/"; // Redirects back to the dashboard after successful registration
    }

    /**
     * Shows the form to create a new emergency request.
     */
    @GetMapping("/request/new")
    public String showNewRequestForm(Model model) {
        model.addAttribute("emergencyRequest", new EmergencyRequest());
        // Pass all existing users for simple dropdown selection (for testing)
        model.addAttribute("users", userService.findAll()); 
        return "request-form"; // Renders src/main/resources/templates/request-form.html
    }

    /**
     * Handles the form submission to create and assign an ambulance request.
     */
    @PostMapping("/request/new")
    public String submitNewRequest(@ModelAttribute EmergencyRequest request, @RequestParam Long requesterId) {
        try {
            // Call the core service logic
            requestService.createAndAssignRequest(
                requesterId, 
                request.getPatientDetails(), 
                request.getDestination()
            );
            // Redirect to dashboard with success message (or just the dashboard)
            return "redirect:/?success=request_assigned"; 
        } catch (IllegalArgumentException e) {
            // Handle error, e.g., redirect back to form with error message
            return "redirect:/request/new?error=" + e.getMessage();
        }
    }

    /**
     * Displays a list of all active (PENDING or ASSIGNED) requests for monitoring.
     */
    @GetMapping("/requests/active")
    public String viewActiveRequests(Model model) {
        // Fetch PENDING and ASSIGNED requests
        List<EmergencyRequest> pendingRequests = requestService.findRequestsByStatus("PENDING");
        List<EmergencyRequest> assignedRequests = requestService.findRequestsByStatus("ASSIGNED");
        
        // Combine them for display
        pendingRequests.addAll(assignedRequests);
        
        model.addAttribute("activeRequests", pendingRequests);
        return "request-list"; // Renders src/main/resources/templates/request-list.html
    }

    /**
     * Handles the completion of an emergency request.
     */
    @PostMapping("/requests/{requestId}/complete")
    public String completeEmergencyRequest(@PathVariable Long requestId) {
        try {
            requestService.completeRequest(requestId);
            // Redirect to active list with success message
            return "redirect:/requests/active?status=completed"; 
        } catch (IllegalArgumentException e) {
            // Redirect back with an error if request not found
            return "redirect:/requests/active?error=" + e.getMessage();
        }
    }

    /**
     * Displays the Ambulance Management view (list and add form).
     */
    @GetMapping("/ambulances/manage")
    public String manageAmbulances(Model model) {
        // List of all ambulances for the table
        model.addAttribute("ambulances", ambulanceService.findAll()); 
        // Blank ambulance object for the 'Add New' form
        model.addAttribute("newAmbulance", new Ambulance()); 
        // List of all possible statuses for the update form dropdowns
        model.addAttribute("allStatuses", AmbulanceStatus.values()); 
        return "ambulance-management"; // Renders src/main/resources/templates/ambulance-management.html
    }
    
    /**
     * Handles submission for adding a new ambulance to the fleet.
     */
    @PostMapping("/ambulances/add")
    public String addAmbulance(@ModelAttribute("newAmbulance") Ambulance ambulance) {
        // Set initial status to AVAILABLE and current time
        ambulance.setStatus(AmbulanceStatus.AVAILABLE);
        ambulance.setLastUpdated(LocalDateTime.now());
        // Set an initial location (e.g., default base coordinates)
        ambulance.setLatitude(12.9716); 
        ambulance.setLongitude(77.5946);
        
        ambulanceService.save(ambulance); // Assuming you have a save method in service
        return "redirect:/ambulances/manage?status=added";
    }

    /**
     * Handles submission for updating the status of an existing ambulance.
     */
    @PostMapping("/ambulances/{id}/update-status")
    public String updateAmbulanceStatus(
            @PathVariable Long id, 
            @RequestParam AmbulanceStatus newStatus) {
        
        // This leverages the existing updateStatusAndLocation method
        // We assume latitude/longitude remain unchanged for a simple status change
        Ambulance ambulance = ambulanceService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ambulance not found."));
            
        ambulanceService.updateStatusAndLocation(
            id, 
            newStatus, 
            ambulance.getLatitude(), 
            ambulance.getLongitude()
        );

        return "redirect:/ambulances/manage?status=updated";
    }
}