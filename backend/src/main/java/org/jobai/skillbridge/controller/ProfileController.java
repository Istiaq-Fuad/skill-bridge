package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.service.ProfileService;
import org.jobai.skillbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    // Main profile endpoints
    @GetMapping("/{userId}")
    public ResponseEntity<Profile> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Profile profile = new Profile();
            profile.setId(userId);
            profile.setUserId(userId);
            profile.setBio(user.getBio());
            profile.setSkills(profileService.getUserSkills(user));
            profile.setEducation(profileService.getUserEducations(user));
            profile.setExperience(profileService.getUserExperiences(user));
            profile.setPortfolio(profileService.getUserPortfolios(user));

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Profile> updateUserProfile(@PathVariable Long userId, @RequestBody Profile profileData,
            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            // Only allow users to update their own profile
            if (!currentUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Update bio if provided
            if (profileData.getBio() != null) {
                user.setBio(profileData.getBio());
                userService.saveUser(user);
            }

            // Return updated profile
            Profile profile = new Profile();
            profile.setId(userId);
            profile.setUserId(userId);
            profile.setBio(user.getBio());
            profile.setSkills(profileService.getUserSkills(user));
            profile.setEducation(profileService.getUserEducations(user));
            profile.setExperience(profileService.getUserExperiences(user));
            profile.setPortfolio(profileService.getUserPortfolios(user));

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

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
    public ResponseEntity<Education> updateEducation(@PathVariable Long id, @RequestBody Education education,
            Authentication authentication) {
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
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience experience,
            Authentication authentication) {
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
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill,
            Authentication authentication) {
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
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio,
            Authentication authentication) {
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

    // User-specific endpoints that match frontend API expectations

    // Skills endpoints
    @PostMapping("/{userId}/skills")
    public ResponseEntity<Skill> addUserSkill(@PathVariable Long userId, @RequestBody Skill skill,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        skill.setUser(currentUser);
        return ResponseEntity.ok(profileService.saveSkill(skill));
    }

    @DeleteMapping("/{userId}/skills/{skillId}")
    public ResponseEntity<Void> removeUserSkill(@PathVariable Long userId, @PathVariable Long skillId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deleteSkill(skillId);
        return ResponseEntity.noContent().build();
    }

    // Education endpoints
    @PostMapping("/{userId}/education")
    public ResponseEntity<Education> addUserEducation(@PathVariable Long userId, @RequestBody Education education,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        education.setUser(currentUser);
        return ResponseEntity.ok(profileService.saveEducation(education));
    }

    @PutMapping("/{userId}/education/{eduId}")
    public ResponseEntity<Education> updateUserEducation(@PathVariable Long userId, @PathVariable Long eduId,
            @RequestBody Education education, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        education.setId(eduId);
        education.setUser(currentUser);
        return ResponseEntity.ok(profileService.saveEducation(education));
    }

    @DeleteMapping("/{userId}/education/{eduId}")
    public ResponseEntity<Void> deleteUserEducation(@PathVariable Long userId, @PathVariable Long eduId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deleteEducation(eduId);
        return ResponseEntity.noContent().build();
    }

    // Experience endpoints
    @PostMapping("/{userId}/experience")
    public ResponseEntity<Experience> addUserExperience(@PathVariable Long userId, @RequestBody Experience experience,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        experience.setUser(currentUser);
        return ResponseEntity.ok(profileService.saveExperience(experience));
    }

    @PutMapping("/{userId}/experience/{expId}")
    public ResponseEntity<Experience> updateUserExperience(@PathVariable Long userId, @PathVariable Long expId,
            @RequestBody Experience experience, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        experience.setId(expId);
        experience.setUser(currentUser);
        return ResponseEntity.ok(profileService.saveExperience(experience));
    }

    @DeleteMapping("/{userId}/experience/{expId}")
    public ResponseEntity<Void> deleteUserExperience(@PathVariable Long userId, @PathVariable Long expId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deleteExperience(expId);
        return ResponseEntity.noContent().build();
    }

    // Portfolio endpoints
    @PostMapping("/{userId}/portfolio")
    public ResponseEntity<Portfolio> addUserPortfolio(@PathVariable Long userId, @RequestBody Portfolio portfolio,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        portfolio.setUser(currentUser);
        return ResponseEntity.ok(profileService.savePortfolio(portfolio));
    }

    @DeleteMapping("/{userId}/portfolio/{portfolioId}")
    public ResponseEntity<Void> deleteUserPortfolio(@PathVariable Long userId, @PathVariable Long portfolioId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        profileService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }
}