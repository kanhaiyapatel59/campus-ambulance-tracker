package com.campus.safety.ambulancetracker.service;

import com.campus.safety.ambulancetracker.model.User;
import com.campus.safety.ambulancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Looking for user: " + username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                System.out.println("❌ User NOT found: " + username);
                return new UsernameNotFoundException("User not found with username: " + username);
            });
        
        System.out.println("✅ User FOUND: " + user.getUsername());
        System.out.println("   - Role: " + user.getRole());
        System.out.println("   - Password hash: " + user.getPassword());
        System.out.println("   - Enabled: " + user.isEnabled());
        
        // Test password matching
        boolean passwordMatches = passwordEncoder.matches("test123", user.getPassword());
        System.out.println("   - Password 'test123' matches: " + passwordMatches);
        
        if (!passwordMatches) {
            System.out.println("❌ PASSWORD MISMATCH!");
            System.out.println("   - Expected: test123");
            System.out.println("   - Stored hash: " + user.getPassword());
            
            // Let's test what the encoded version of test123 should be
            String testEncoded = passwordEncoder.encode("test123");
            System.out.println("   - Current encode('test123'): " + testEncoded);
        }
        
        return user;
    }
}