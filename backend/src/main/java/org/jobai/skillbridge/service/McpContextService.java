package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Portfolio;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.EducationRepository;
import org.jobai.skillbridge.repo.ExperienceRepository;
import org.jobai.skillbridge.repo.PortfolioRepository;
import org.jobai.skillbridge.repo.SkillRepository;
import org.jobai.skillbridge.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
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
     * Helper method to get field value using reflection
     * @param obj The object to get the field value from
     * @param fieldName The name of the field
     * @return The field value or null if not found
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // If we can't access the field directly, try with getter method
            try {
                String capitalizedFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String getterName = "get" + capitalizedFieldName;
                java.lang.reflect.Method method = obj.getClass().getMethod(getterName);
                return method.invoke(obj);
            } catch (Exception ex) {
                System.err.println("Could not access field " + fieldName + " in " + obj.getClass().getName());
                return null;
            }
        }
    }
    
    /**
     * Generate context for user profile
     * @param userId The user ID
     * @return Map containing user profile context
     */
    public Map<String, Object> generateUserProfileContext(Long userId) {
        Map<String, Object> context = new HashMap<>();
        
        // Fetch user data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Add comprehensive personal info
        Map<String, Object> personalInfo = new HashMap<>();
        personalInfo.put("name", getFieldValue(user, "firstName") + " " + getFieldValue(user, "lastName"));
        personalInfo.put("email", getFieldValue(user, "email"));
        personalInfo.put("phone", getFieldValue(user, "phoneNumber"));
        personalInfo.put("address", getFieldValue(user, "address"));
        personalInfo.put("city", getFieldValue(user, "city"));
        personalInfo.put("country", getFieldValue(user, "country"));
        personalInfo.put("bio", getFieldValue(user, "bio"));
        personalInfo.put("username", getFieldValue(user, "username"));
        context.put("personal_info", personalInfo);
        
        // Add education history with more details
        List<Map<String, Object>> educations = new ArrayList<>();
        for (Education edu : educationRepository.findByUser(user)) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("institution", getFieldValue(edu, "institution"));
            eduMap.put("degree", getFieldValue(edu, "degree"));
            eduMap.put("field_of_study", getFieldValue(edu, "fieldOfStudy"));
            eduMap.put("start_date", getFieldValue(edu, "startDate"));
            eduMap.put("end_date", getFieldValue(edu, "endDate"));
            eduMap.put("grade", getFieldValue(edu, "grade"));
            eduMap.put("description", getFieldValue(edu, "description"));
            // Add any additional education fields
            educations.add(eduMap);
        }
        context.put("education", educations);
        
        // Add work experience with more details
        List<Map<String, Object>> experiences = new ArrayList<>();
        for (Experience exp : experienceRepository.findByUser(user)) {
            Map<String, Object> expMap = new HashMap<>();
            expMap.put("company", getFieldValue(exp, "company"));
            expMap.put("position", getFieldValue(exp, "position"));
            expMap.put("description", getFieldValue(exp, "description"));
            expMap.put("start_date", getFieldValue(exp, "startDate"));
            expMap.put("end_date", getFieldValue(exp, "endDate"));
            expMap.put("currently_working", getFieldValue(exp, "currentlyWorking"));
            // Add any additional experience fields
            experiences.add(expMap);
        }
        context.put("experience", experiences);
        
        // Add skills with more details
        List<Map<String, Object>> skills = new ArrayList<>();
        for (Skill skill : skillRepository.findByUser(user)) {
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("name", getFieldValue(skill, "name"));
            skillMap.put("category", getFieldValue(skill, "category"));
            skillMap.put("proficiency_level", getFieldValue(skill, "proficiencyLevel"));
            // Add any additional skill fields
            skills.add(skillMap);
        }
        context.put("skills", skills);
        
        // Add portfolio with more details
        List<Map<String, Object>> portfolios = new ArrayList<>();
        for (Portfolio portfolio : portfolioRepository.findByUser(user)) {
            Map<String, Object> portfolioMap = new HashMap<>();
            portfolioMap.put("title", getFieldValue(portfolio, "title"));
            portfolioMap.put("description", getFieldValue(portfolio, "description"));
            portfolioMap.put("url", getFieldValue(portfolio, "url"));
            portfolioMap.put("media_type", getFieldValue(portfolio, "mediaType"));
            // Add any additional portfolio fields
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