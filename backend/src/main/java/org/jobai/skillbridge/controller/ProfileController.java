package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.service.ProfileService;
import org.jobai.skillbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private UserService userService;
    
    // Education endpoints
    @GetMapping("/education")
    public ResponseEntity<List<Education>> getUserEducations(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getUserEducations(user));
    }
    
    @PostMapping("/education")
    public ResponseEntity<Education> addEducation(@RequestBody Education education, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        education.setUser(user);
        return ResponseEntity.ok(profileService.saveEducation(education));
    }
    
    @PutMapping("/education/{id}")
    public ResponseEntity<Education> updateEducation(@PathVariable Long id, @RequestBody Education education, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        education.setId(id);
        education.setUser(user);
        return ResponseEntity.ok(profileService.saveEducation(education));
    }
    
    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {
        profileService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }
    
    // Experience endpoints
    @GetMapping("/experience")
    public ResponseEntity<List<Experience>> getUserExperiences(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getUserExperiences(user));
    }
    
    @PostMapping("/experience")
    public ResponseEntity<Experience> addExperience(@RequestBody Experience experience, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        experience.setUser(user);
        return ResponseEntity.ok(profileService.saveExperience(experience));
    }
    
    @PutMapping("/experience/{id}")
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience experience, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        experience.setId(id);
        experience.setUser(user);
        return ResponseEntity.ok(profileService.saveExperience(experience));
    }
    
    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        profileService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
    
    // Skill endpoints
    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getUserSkills(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getUserSkills(user));
    }
    
    @PostMapping("/skills")
    public ResponseEntity<Skill> addSkill(@RequestBody Skill skill, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        skill.setUser(user);
        return ResponseEntity.ok(profileService.saveSkill(skill));
    }
    
    @PutMapping("/skills/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        skill.setId(id);
        skill.setUser(user);
        return ResponseEntity.ok(profileService.saveSkill(skill));
    }
    
    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        profileService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
    
    // Portfolio endpoints
    @GetMapping("/portfolio")
    public ResponseEntity<List<Portfolio>> getUserPortfolios(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getUserPortfolios(user));
    }
    
    @PostMapping("/portfolio")
    public ResponseEntity<Portfolio> addPortfolio(@RequestBody Portfolio portfolio, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        portfolio.setUser(user);
        return ResponseEntity.ok(profileService.savePortfolio(portfolio));
    }
    
    @PutMapping("/portfolio/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        portfolio.setId(id);
        portfolio.setUser(user);
        return ResponseEntity.ok(profileService.savePortfolio(portfolio));
    }
    
    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        profileService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}