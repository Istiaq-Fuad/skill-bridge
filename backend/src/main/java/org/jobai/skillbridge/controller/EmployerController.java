package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.EmployerProfile;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.EmployerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer")
public class EmployerController {
    
    @Autowired
    private EmployerProfileService employerProfileService;
    
    @GetMapping("/profile")
    public ResponseEntity<EmployerProfile> getEmployerProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return employerProfileService.getEmployerProfileByUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/profile")
    public ResponseEntity<EmployerProfile> createEmployerProfile(
            @RequestBody EmployerProfile employerProfile, 
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        employerProfile.setUser(user);
        EmployerProfile savedProfile = employerProfileService.saveEmployerProfile(employerProfile);
        return ResponseEntity.ok(savedProfile);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<EmployerProfile> updateEmployerProfile(
            @RequestBody EmployerProfile employerProfile, 
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        employerProfile.setUser(user);
        EmployerProfile savedProfile = employerProfileService.saveEmployerProfile(employerProfile);
        return ResponseEntity.ok(savedProfile);
    }
    
    @DeleteMapping("/profile/{id}")
    public ResponseEntity<Void> deleteEmployerProfile(@PathVariable Long id) {
        employerProfileService.deleteEmployerProfile(id);
        return ResponseEntity.noContent().build();
    }
}