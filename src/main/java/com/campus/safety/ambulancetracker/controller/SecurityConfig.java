package com.campus.safety.ambulancetracker.controller;

import com.campus.safety.ambulancetracker.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/register"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/webjars/**")
                ).permitAll()
                
                // Admin only endpoints
                .requestMatchers(
                    new AntPathRequestMatcher("/ambulances/manage"),
                    new AntPathRequestMatcher("/ambulances/add"),
                    new AntPathRequestMatcher("/ambulances/**/update-status"),
                    new AntPathRequestMatcher("/reports"),
                    new AntPathRequestMatcher("/api/**")
                ).hasRole("ADMIN")
                
                // User accessible endpoints
                .requestMatchers(
                    new AntPathRequestMatcher("/dashboard"),
                    new AntPathRequestMatcher("/request/new"),
                    new AntPathRequestMatcher("/requests/active"),
                    new AntPathRequestMatcher("/requests/**/complete")
                ).hasAnyRole("USER", "ADMIN", "STUDENT", "STAFF", "SECURITY")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}
