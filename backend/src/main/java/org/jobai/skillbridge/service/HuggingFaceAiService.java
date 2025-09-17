package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class HuggingFaceAiService {
    
    @Value("${huggingface.api.token}")
    private String apiToken;
    
    @Value("${huggingface.model.name:google/flan-t5-small}")
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
        try {
            // Get structured context using MCP
            Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
            
            // Create prompt for resume generation
            String prompt = buildResumePrompt(context, jobTitle);
            
            // Call Hugging Face API
            return callHuggingFaceApi(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating resume: " + e.getMessage();
        }
    }
    
    /**
     * Optimize resume for a specific job
     * @param userId The user ID
     * @param jobId The job ID
     * @return Optimized resume content
     */
    public String optimizeResumeForJob(Long userId, Integer jobId) {
        try {
            // Get job-specific context using MCP
            Map<String, Object> context = mcpContextService.generateJobOptimizationContext(userId, jobId);
            
            // Create prompt for job-specific optimization
            String prompt = buildJobOptimizationPrompt(context, jobId);
            
            // Call Hugging Face API
            return callHuggingFaceApi(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error optimizing resume: " + e.getMessage();
        }
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
        System.out.println("Hugging Face API Token: " + apiToken);
        try {
            // Validate API token
            if (apiToken == null || apiToken.trim().isEmpty()) {
                return "Error: Hugging Face API token is not configured. Please set HUGGINGFACE_API_TOKEN in your .env file.";
            }
            
            // Validate model name
            if (modelName == null || modelName.trim().isEmpty()) {
                return "Error: Hugging Face model name is not configured.";
            }
            
            RestTemplate restTemplate = new RestTemplate();
            
            // Construct URL
            String url = HUGGING_FACE_API_URL + modelName.trim();
            System.out.println("Calling Hugging Face API at URL: " + url);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken.trim());
            
            // Create request body - Simplified for Hugging Face API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            
            // Add parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_new_tokens", 200);
            parameters.put("temperature", 0.7);
            requestBody.put("parameters", parameters);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            System.out.println("Sending request to Hugging Face API with prompt length: " + prompt.length());
            
            System.out.println("Request body: " + new ObjectMapper().writeValueAsString(requestBody));
            
            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("Received response from Hugging Face API with status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            // Handle different response statuses
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "Error: Model '" + modelName + "' not found. Please check the model name in your configuration.";
            }
            
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "Error: Invalid Hugging Face API token. Please check your HUGGINGFACE_API_TOKEN in .env file.";
            }
            
            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "Error: Access forbidden to Hugging Face model '" + modelName + "'. This might be due to model permissions or rate limiting.";
            }
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error calling Hugging Face API. Status: " + response.getStatusCode() + ". Response: " + response.getBody();
            }
            
            // Parse response
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                // Handle different response formats
                if (root.isArray() && root.size() > 0) {
                    JsonNode firstResult = root.get(0);
                    if (firstResult.has("generated_text")) {
                        return firstResult.get("generated_text").asText();
                    }
                } else if (root.has("generated_text")) {
                    return root.get("generated_text").asText();
                }
                
                // If we can't parse as JSON, return raw response
                return response.getBody();
                
            } catch (Exception jsonParseError) {
                // If JSON parsing fails, return the raw response
                System.out.println("JSON parsing failed, returning raw response: " + response.getBody());
                return response.getBody();
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("404 Not Found error when calling Hugging Face API");
            System.err.println("Model: " + modelName);
            System.err.println("URL: " + HUGGING_FACE_API_URL + modelName.trim());
            e.printStackTrace();
            return "Error: Model '" + modelName + "' not found (404). Please check the model name in your configuration. Available models: google/flan-t5-small, distilgpt2, gpt2";
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("401 Unauthorized error when calling Hugging Face API");
            System.err.println("Token: " + (apiToken != null ? "Provided" : "Not provided"));
            e.printStackTrace();
            return "Error: Invalid Hugging Face API token (401). Please check your HUGGINGFACE_API_TOKEN in .env file.";
        } catch (HttpClientErrorException.Forbidden e) {
            System.err.println("403 Forbidden error when calling Hugging Face API");
            e.printStackTrace();
            return "Error: Access forbidden to Hugging Face model '" + modelName + "' (403). This might be due to model permissions or rate limiting.";
        } catch (Exception e) {
            System.err.println("General error when calling Hugging Face API");
            e.printStackTrace();
            return "Error generating resume: " + e.getMessage();
        }
    }
}