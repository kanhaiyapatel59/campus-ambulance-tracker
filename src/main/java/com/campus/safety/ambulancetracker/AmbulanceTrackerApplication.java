package com.campus.safety.ambulancetracker;

import com.campus.safety.ambulancetracker.model.Ambulance;
import com.campus.safety.ambulancetracker.model.AmbulanceStatus;
import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.AmbulanceRepository;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class AmbulanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmbulanceTrackerApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initData(AmbulanceRepository ambulanceRepository, 
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // Create ambulances if empty
            if (ambulanceRepository.count() == 0) {
                Ambulance a1 = new Ambulance(
                    null, "KA01MT9988", "Ravi Kumar", "9876512340",
                    AmbulanceStatus.AVAILABLE, 12.9716, 77.5946, 
                    LocalDateTime.now(), null
                );
                
                Ambulance a2 = new Ambulance(
                    null, "KA01MT1122", "Suresh Reddy", "9876512341",
                    AmbulanceStatus.OUT_OF_SERVICE, 12.9750, 77.5980, 
                    LocalDateTime.now(), null
                );

                ambulanceRepository.save(a1);
                ambulanceRepository.save(a2);
                System.out.println(">>> 2 test ambulances created on startup. <<<");
            } else {
                System.out.println(">>> Skipping test data creation: Ambulances already exist. <<<");
            }

            // Create default users if empty
            if (userRepository.count() == 0) {
                // Create ADMIN user
                User admin = new User();
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setEmail("admin@campus.edu");
                admin.setContactNumber("9876543210");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Fresh encoding
                admin.setRole("ADMIN");
                userRepository.save(admin);

                // Create regular USER
                User user = new User();
                user.setFirstName("John");
                user.setLastName("Doe");
                user.setEmail("john.doe@campus.edu");
                user.setContactNumber("9876543211");
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123")); // Fresh encoding
                user.setRole("USER");
                userRepository.save(user);

                System.out.println(">>> Default users created:");
                System.out.println(">>> ADMIN - Username: admin, Password: admin123");
                System.out.println(">>> USER  - Username: user, Password: user123");
            } else {
                System.out.println(">>> Users already exist in database.");
                // Update existing users with properly encoded passwords
                User admin = userRepository.findByUsername("admin").orElse(null);
                if (admin != null) {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(admin);
                    System.out.println(">>> Updated admin password");
                }
                
                User user = userRepository.findByUsername("user").orElse(null);
                if (user != null) {
                    user.setPassword(passwordEncoder.encode("user123"));
                    userRepository.save(user);
                    System.out.println(">>> Updated user password");
                }
            }
        };
    }
}