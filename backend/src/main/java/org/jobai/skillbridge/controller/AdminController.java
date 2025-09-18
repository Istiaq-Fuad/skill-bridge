package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.AdminProfile;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.AdminProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminProfileService adminProfileService;
    
    @GetMapping("/profile")
    public ResponseEntity<AdminProfile> getAdminProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return adminProfileService.getAdminProfileByUser(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/profile")
    public ResponseEntity<AdminProfile> createAdminProfile(
            @RequestBody AdminProfile adminProfile, 
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        adminProfile.setUser(user);
        AdminProfile savedProfile = adminProfileService.saveAdminProfile(adminProfile);
        return ResponseEntity.ok(savedProfile);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<AdminProfile> updateAdminProfile(
            @RequestBody AdminProfile adminProfile, 
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        adminProfile.setUser(user);
        AdminProfile savedProfile = adminProfileService.saveAdminProfile(adminProfile);
        return ResponseEntity.ok(savedProfile);
    }
    
    @DeleteMapping("/profile/{id}")
    public ResponseEntity<Void> deleteAdminProfile(@PathVariable Long id) {
        adminProfileService.deleteAdminProfile(id);
        return ResponseEntity.noContent().build();
    }
}