package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.HuggingFaceAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private HuggingFaceAiService aiService;
    
    /**
     * Generate a resume for the authenticated user
     * @param jobTitle Optional job title to tailor the resume for
     * @param authentication Authentication object containing user info
     * @return Generated resume content
     */
    @PostMapping("/resume/generate")
    public ResponseEntity<String> generateResume(
            @RequestParam(required = false) String jobTitle,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            String resume = aiService.generateResume(user.getId(), jobTitle);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating resume: " + e.getMessage());
        }
    }
    
    /**
     * Optimize resume for a specific job
     * @param jobId The job ID to optimize for
     * @param authentication Authentication object containing user info
     * @return Optimized resume content
     */
    @PostMapping("/resume/optimize/{jobId}")
    public ResponseEntity<String> optimizeResumeForJob(
            @PathVariable Integer jobId,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            String optimizedResume = aiService.optimizeResumeForJob(user.getId(), jobId);
            return ResponseEntity.ok(optimizedResume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error optimizing resume: " + e.getMessage());
        }
    }
    
    /**
     * Get MCP context for debugging purposes
     * @param authentication Authentication object containing user info
     * @return User profile context
     */
    @GetMapping("/context/profile")
    public ResponseEntity<Object> getProfileContext(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            // This is a placeholder - you'd need to expose the context service properly
            return ResponseEntity.ok("Context generation endpoint");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating context: " + e.getMessage());
        }
    }
}