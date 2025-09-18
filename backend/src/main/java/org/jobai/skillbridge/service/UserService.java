package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.repo.UserRepository;
import org.jobai.skillbridge.repo.SkillRepository;
import org.jobai.skillbridge.repo.ExperienceRepository;
import org.jobai.skillbridge.repo.EducationRepository;
import org.jobai.skillbridge.service.ResumeParsingService.ParsedResumeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private ExperienceRepository experienceRepository;
    
    @Autowired
    private EducationRepository educationRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    /**
     * Update user profile with parsed resume data
     * @param userId The user ID to update
     * @param parsedData The parsed resume data
     * @return Updated user
     */
    public User updateUserProfileWithResumeData(Long userId, ParsedResumeData parsedData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Update basic profile information if provided
        if (parsedData.getName() != null && !parsedData.getName().isEmpty()) {
            String[] names = parsedData.getName().split(" ", 2);
            user.setFirstName(names[0]);
            if (names.length > 1) {
                user.setLastName(names[1]);
            }
        }
        
        if (parsedData.getEmail() != null && !parsedData.getEmail().isEmpty()) {
            user.setEmail(parsedData.getEmail());
        }
        
        if (parsedData.getPhone() != null && !parsedData.getPhone().isEmpty()) {
            user.setPhoneNumber(parsedData.getPhone());
        }
        
        if (parsedData.getSummary() != null && !parsedData.getSummary().isEmpty()) {
            user.setBio(parsedData.getSummary());
        }
        
        // Save updated user
        User updatedUser = userRepository.save(user);
        
        // Update skills
        updateSkills(userId, parsedData.getSkills());
        
        // Update experiences
        updateExperiences(userId, parsedData.getExperiences());
        
        // Update education
        updateEducations(userId, parsedData.getEducations());
        
        return updatedUser;
    }
    
    /**
     * Update user skills with parsed data
     * @param userId The user ID
     * @param skills The parsed skills
     */
    private void updateSkills(Long userId, List<Skill> skills) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Clear existing skills
        skillRepository.deleteByUser(user);
        
        // Add new skills
        for (Skill skill : skills) {
            skill.setUser(user);
            skillRepository.save(skill);
        }
    }
    
    /**
     * Update user experiences with parsed data
     * @param userId The user ID
     * @param experiences The parsed experiences
     */
    private void updateExperiences(Long userId, List<Experience> experiences) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Clear existing experiences
        experienceRepository.deleteByUser(user);
        
        // Add new experiences
        for (Experience experience : experiences) {
            experience.setUser(user);
            experienceRepository.save(experience);
        }
    }
    
    /**
     * Update user educations with parsed data
     * @param userId The user ID
     * @param educations The parsed educations
     */
    private void updateEducations(Long userId, List<Education> educations) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Clear existing educations
        educationRepository.deleteByUser(user);
        
        // Add new educations
        for (Education education : educations) {
            education.setUser(user);
            educationRepository.save(education);
        }
    }
}