package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.service.ProfileService;
import org.jobai.skillbridge.service.UserService;
import org.jobai.skillbridge.util.ReflectionUtils;
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
        List<Education> educations = profileService.getUserEducations(user);
        return ResponseEntity.ok(educations);
    }
    
    @PostMapping("/education")
    public ResponseEntity<Education> addEducation(@RequestBody Education education, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(education, "user", user);
        Education savedEducation = profileService.saveEducation(education);
        return ResponseEntity.ok(savedEducation);
    }
    
    @PutMapping("/education/{id}")
    public ResponseEntity<Education> updateEducation(@PathVariable Long id, @RequestBody Education education, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(education, "id", id);
        ReflectionUtils.setFieldValue(education, "user", user);
        Education savedEducation = profileService.saveEducation(education);
        return ResponseEntity.ok(savedEducation);
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
        List<Experience> experiences = profileService.getUserExperiences(user);
        return ResponseEntity.ok(experiences);
    }
    
    @PostMapping("/experience")
    public ResponseEntity<Experience> addExperience(@RequestBody Experience experience, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(experience, "user", user);
        Experience savedExperience = profileService.saveExperience(experience);
        return ResponseEntity.ok(savedExperience);
    }
    
    @PutMapping("/experience/{id}")
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience experience, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(experience, "id", id);
        ReflectionUtils.setFieldValue(experience, "user", user);
        Experience savedExperience = profileService.saveExperience(experience);
        return ResponseEntity.ok(savedExperience);
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
        List<Skill> skills = profileService.getUserSkills(user);
        return ResponseEntity.ok(skills);
    }
    
    @PostMapping("/skills")
    public ResponseEntity<Skill> addSkill(@RequestBody Skill skill, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(skill, "user", user);
        Skill savedSkill = profileService.saveSkill(skill);
        return ResponseEntity.ok(savedSkill);
    }
    
    @PutMapping("/skills/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(skill, "id", id);
        ReflectionUtils.setFieldValue(skill, "user", user);
        Skill savedSkill = profileService.saveSkill(skill);
        return ResponseEntity.ok(savedSkill);
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
        List<Portfolio> portfolios = profileService.getUserPortfolios(user);
        return ResponseEntity.ok(portfolios);
    }
    
    @PostMapping("/portfolio")
    public ResponseEntity<Portfolio> addPortfolio(@RequestBody Portfolio portfolio, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(portfolio, "user", user);
        Portfolio savedPortfolio = profileService.savePortfolio(portfolio);
        return ResponseEntity.ok(savedPortfolio);
    }
    
    @PutMapping("/portfolio/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReflectionUtils.setFieldValue(portfolio, "id", id);
        ReflectionUtils.setFieldValue(portfolio, "user", user);
        Portfolio savedPortfolio = profileService.savePortfolio(portfolio);
        return ResponseEntity.ok(savedPortfolio);
    }
    
    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        profileService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}