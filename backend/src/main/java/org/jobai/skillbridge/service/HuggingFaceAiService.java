package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class HuggingFaceAiService {
    
    @Value("${huggingface.api.token}")
    private String apiToken;
    
    @Value("${huggingface.model.name:gpt2}")
    private String modelName;
    
    @Autowired
    private McpContextService mcpContextService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String HUGGING_FACE_API_URL = "https://api-inference.huggingface.co/models/";
    
    /**
     * Generate a resume for a user
     * @param userId The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @return Generated resume content
     */
    public String generateResume(Long userId, String jobTitle) {
        // Get structured context using MCP
        Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
        
        // Create prompt for resume generation
        String prompt = buildResumePrompt(context, jobTitle);
        
        // Call Hugging Face API
        return callHuggingFaceApi(prompt);
    }
    
    /**
     * Optimize resume for a specific job
     * @param userId The user ID
     * @param jobId The job ID
     * @return Optimized resume content
     */
    public String optimizeResumeForJob(Long userId, Integer jobId) {
        // Get job-specific context using MCP
        Map<String, Object> context = mcpContextService.generateJobOptimizationContext(userId, jobId);
        
        // Create prompt for job-specific optimization
        String prompt = buildJobOptimizationPrompt(context, jobId);
        
        // Call Hugging Face API
        return callHuggingFaceApi(prompt);
    }
    
    /**
     * Build prompt for resume generation
     * @param context MCP context data
     * @param jobTitle Optional job title
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional resume for a ");
        if (jobTitle != null && !jobTitle.isEmpty()) {
            prompt.append(jobTitle).append(" based on the following profile:\n\n");
        } else {
            prompt.append("professional based on the following profile:\n\n");
        }
        
        // Format context data into readable prompt
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
            prompt.append("Name: ").append(personalInfo.get("name")).append("\n");
            prompt.append("Email: ").append(personalInfo.get("email")).append("\n");
            if (personalInfo.get("phone") != null) {
                prompt.append("Phone: ").append(personalInfo.get("phone")).append("\n");
            }
            prompt.append("Location: ").append(personalInfo.get("city")).append(", ")
                  .append(personalInfo.get("country")).append("\n\n");
        }
        
        // Add bio if available
        if (personalInfo != null && personalInfo.get("bio") != null) {
            prompt.append("Professional Summary:\n").append(personalInfo.get("bio")).append("\n\n");
        }
        
        // Add education
        prompt.append("Education:\n");
        List<Map<String, Object>> educations = (List<Map<String, Object>>) context.get("education");
        if (educations != null) {
            for (Map<String, Object> edu : educations) {
                prompt.append("- ").append(edu.get("degree")).append(" in ")
                      .append(edu.get("field_of_study")).append(" from ")
                      .append(edu.get("institution")).append("\n");
            }
        }
        prompt.append("\n");
        
        // Add experience
        prompt.append("Work Experience:\n");
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null) {
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append(" at ")
                      .append(exp.get("company")).append("\n");
            }
        }
        prompt.append("\n");
        
        // Add skills
        prompt.append("Skills:\n");
        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null) {
            for (Map<String, Object> skill : skills) {
                prompt.append("- ").append(skill.get("name")).append(" (Level: ")
                      .append(skill.get("proficiency_level")).append("/10)\n");
            }
        }
        
        prompt.append("\n\nPlease generate a well-formatted, professional resume based on this information.");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for job optimization
     * @param context MCP context data
     * @param jobId Job ID
     * @return Formatted prompt
     */
    private String buildJobOptimizationPrompt(Map<String, Object> context, Integer jobId) {
        // For now, we'll use the same prompt structure but this can be enhanced
        // to include job description details when you implement job fetching
        return buildResumePrompt(context, null) + 
               "\n\nPlease optimize this resume specifically for job ID: " + jobId;
    }
    
    /**
     * Call Hugging Face Inference API
     * @param prompt The prompt to send
     * @return Generated text response
     */
    private String callHuggingFaceApi(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);
            
            // Create request body
            String requestBody = "{\n" +
                    "  \"inputs\": \"" + prompt.replace("\"", "\\\"") + "\",\n" +
                    "  \"parameters\": {\n" +
                    "    \"max_new_tokens\": 500,\n" +
                    "    \"temperature\": 0.7\n" +
                    "  }\n" +
                    "}";
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            // Make API call
            String url = HUGGING_FACE_API_URL + modelName;
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("generated_text")) {
                return root.get("generated_text").asText();
            }
            
            return "Failed to generate resume. API Response: " + response.getBody();
            
        } catch (Exception e) {
            return "Error generating resume: " + e.getMessage();
        }
    }
}