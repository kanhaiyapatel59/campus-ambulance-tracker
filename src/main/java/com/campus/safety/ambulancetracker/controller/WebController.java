package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;
import com.campus.safety.ambulancetracker.model.EmergencyRequest;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.service.AmbulanceService;
import com.campus.safety.ambulancetracker.service.EmergencyRequestService;
import com.campus.safety.ambulancetracker.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final EmergencyRequestService requestService;
    private final AmbulanceService ambulanceService;
    private final UserService userService;

    public WebController(AmbulanceService ambulanceService,
                         UserService userService,
                         EmergencyRequestService requestService) {
        this.ambulanceService = ambulanceService;
        this.userService = userService;
        this.requestService = requestService;
    }

    /**
     * Displays the main operational dashboard.
     */
    @GetMapping({"/", "/dashboard"})
        public String dashboard(Model model)  {
        model.addAttribute("ambulances", ambulanceService.findAll());
        return "dashboard"; // src/main/resources/templates/dashboard.html
    }

    /**
     * Displays the user registration form.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user-register";
    }

    /**
     * Handles user registration form submission.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/";
    }

    /**
     * Shows the form to create a new emergency request.
     */
    @GetMapping("/request/new")
    public String showNewRequestForm(Model model) {
        model.addAttribute("emergencyRequest", new EmergencyRequest());
        model.addAttribute("users", userService.findAll());
        return "request-form";
    }

    /**
     * Handles submission of a new emergency request.
     */
    @PostMapping("/request/new")
    public String submitNewRequest(@ModelAttribute EmergencyRequest request, @RequestParam Long requesterId) {
        try {
            requestService.createAndAssignRequest(
                    requesterId,
                    request.getPatientDetails(),
                    request.getDestination()
            );
            return "redirect:/?success=request_assigned";
        } catch (IllegalArgumentException e) {
            return "redirect:/request/new?error=" + e.getMessage();
        }
    }

    /**
     * Displays all active (PENDING + ASSIGNED) emergency requests.
     */
    @GetMapping("/requests/active")
    public String viewActiveRequests(Model model) {
        List<EmergencyRequest> pendingRequests = requestService.findRequestsByStatus("PENDING");
        List<EmergencyRequest> assignedRequests = requestService.findRequestsByStatus("ASSIGNED");
        pendingRequests.addAll(assignedRequests);

        model.addAttribute("activeRequests", pendingRequests);
        return "request-list";
    }

    /**
     * Marks an emergency request as completed.
     */
    @PostMapping("/requests/{requestId}/complete")
    public String completeEmergencyRequest(@PathVariable Long requestId) {
        try {
            requestService.completeRequest(requestId);
            return "redirect:/requests/active?status=completed";
        } catch (IllegalArgumentException e) {
            return "redirect:/requests/active?error=" + e.getMessage();
        }
    }

    /**
     * Displays the Ambulance Management view (list and add form).
     */
    @GetMapping("/ambulances/manage")
    public String manageAmbulances(Model model) {
        model.addAttribute("ambulances", ambulanceService.findAll());
        model.addAttribute("newAmbulance", new Ambulance());
        model.addAttribute("allStatuses", AmbulanceStatus.values());
        return "ambulance-management";
    }

    /**
     * Handles adding a new ambulance.
     */
    @PostMapping("/ambulances/add")
    public String addAmbulance(@ModelAttribute("newAmbulance") Ambulance ambulance) {
        ambulance.setStatus(AmbulanceStatus.AVAILABLE);
        ambulance.setLastUpdated(LocalDateTime.now());
        ambulance.setLatitude(12.9716);
        ambulance.setLongitude(77.5946);

        ambulanceService.save(ambulance);
        return "redirect:/ambulances/manage?status=added";
    }

    /**
     * Handles updating ambulance status.
     */
    @PostMapping("/ambulances/{id}/update-status")
    public String updateAmbulanceStatus(@PathVariable Long id, @RequestParam AmbulanceStatus newStatus) {
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

    /**
     * Displays the analytics and reporting dashboard.
     */
    @GetMapping("/reports")
    public String viewReports(Model model) {
        Map<String, String> reports = requestService.generateReports();
        model.addAllAttributes(reports);
        return "reports"; // src/main/resources/templates/reports.html
    }
}