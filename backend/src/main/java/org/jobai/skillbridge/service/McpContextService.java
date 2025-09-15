package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class McpContextService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EducationRepository educationRepository;
    
    @Autowired
    private ExperienceRepository experienceRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Generate MCP context for a user profile
     * @param userId The user ID to generate context for
     * @return Map containing structured context data
     */
    public Map<String, Object> generateUserProfileContext(Long userId) {
        Map<String, Object> context = new HashMap<>();
        
        // Fetch user data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Add basic user info
        Map<String, Object> personalInfo = new HashMap<>();
        personalInfo.put("name", user.getFirstName() + " " + user.getLastName());
        personalInfo.put("email", user.getEmail());
        personalInfo.put("phone", user.getPhoneNumber());
        personalInfo.put("address", user.getAddress());
        personalInfo.put("city", user.getCity());
        personalInfo.put("country", user.getCountry());
        personalInfo.put("bio", user.getBio());
        context.put("personal_info", personalInfo);
        
        // Add education history
        List<Map<String, Object>> educations = new ArrayList<>();
        for (org.jobai.skillbridge.model.Education edu : educationRepository.findByUser(user)) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("institution", edu.getInstitution());
            eduMap.put("degree", edu.getDegree());
            eduMap.put("field_of_study", edu.getFieldOfStudy());
            eduMap.put("start_date", edu.getStartDate());
            eduMap.put("end_date", edu.getEndDate());
            eduMap.put("grade", edu.getGrade());
            eduMap.put("description", edu.getDescription());
            educations.add(eduMap);
        }
        context.put("education", educations);
        
        // Add work experience
        List<Map<String, Object>> experiences = new ArrayList<>();
        for (org.jobai.skillbridge.model.Experience exp : experienceRepository.findByUser(user)) {
            Map<String, Object> expMap = new HashMap<>();
            expMap.put("company", exp.getCompany());
            expMap.put("position", exp.getPosition());
            expMap.put("description", exp.getDescription());
            expMap.put("start_date", exp.getStartDate());
            expMap.put("end_date", exp.getEndDate());
            expMap.put("currently_working", exp.isCurrentlyWorking());
            experiences.add(expMap);
        }
        context.put("experience", experiences);
        
        // Add skills
        List<Map<String, Object>> skills = new ArrayList<>();
        for (org.jobai.skillbridge.model.Skill skill : skillRepository.findByUser(user)) {
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("name", skill.getName());
            skillMap.put("category", skill.getCategory());
            skillMap.put("proficiency_level", skill.getProficiencyLevel());
            skills.add(skillMap);
        }
        context.put("skills", skills);
        
        // Add portfolio
        List<Map<String, Object>> portfolios = new ArrayList<>();
        for (org.jobai.skillbridge.model.Portfolio portfolio : portfolioRepository.findByUser(user)) {
            Map<String, Object> portfolioMap = new HashMap<>();
            portfolioMap.put("title", portfolio.getTitle());
            portfolioMap.put("description", portfolio.getDescription());
            portfolioMap.put("url", portfolio.getUrl());
            portfolioMap.put("media_type", portfolio.getMediaType());
            portfolios.add(portfolioMap);
        }
        context.put("portfolio", portfolios);
        
        return context;
    }
    
    /**
     * Generate context for job-specific resume optimization
     * @param userId The user ID
     * @param jobId The job ID
     * @return Map containing job-specific context
     */
    public Map<String, Object> generateJobOptimizationContext(Long userId, Integer jobId) {
        // This would include job details and user profile
        Map<String, Object> context = generateUserProfileContext(userId);
        
        // Add job-specific information here
        // You would fetch job details from your JobRepo
        
        return context;
    }
}