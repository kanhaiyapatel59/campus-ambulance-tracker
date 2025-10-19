package com.campus.safety.ambulancetracker;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.LocalDateTime;

@SpringBootApplication
public class AmbulanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmbulanceTrackerApplication.class, args);
    }
    
    // This bean will run after the application starts
    @Bean
    public CommandLineRunner initData(AmbulanceRepository ambulanceRepository) {
        return args -> {
            // FIX: Only insert test data if the repository is empty
            if (ambulanceRepository.count() == 0) {
                // 1. Create a fully AVAILABLE ambulance
                Ambulance a1 = new Ambulance(
                    null, // id (auto-generated)
                    "KA01MT9988", 
                    "Ravi Kumar", 
                    "9876512340",
                    AmbulanceStatus.AVAILABLE,
                    12.9716,  // Placeholder Latitude (e.g., Campus Center)
                    77.5946,  // Placeholder Longitude
                    LocalDateTime.now(),
                    null // emergencyRequests list
                );
                
                // 2. Create an ambulance that is OUT_OF_SERVICE
                Ambulance a2 = new Ambulance(
                    null, 
                    "KA01MT1122", 
                    "Suresh Reddy", 
                    "9876512341",
                    AmbulanceStatus.OUT_OF_SERVICE,
                    12.9750, 
                    77.5980, 
                    LocalDateTime.now(),
                    null
                );

                ambulanceRepository.save(a1);
                ambulanceRepository.save(a2);
                System.out.println(">>> 2 test ambulances created on startup. <<<");
            } else {
                System.out.println(">>> Skipping test data creation: Ambulances already exist. <<<");
            }
        };
    }
}