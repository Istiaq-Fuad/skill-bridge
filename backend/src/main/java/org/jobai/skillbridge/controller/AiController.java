package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.MistralAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private MistralAiService aiService;
    
    /**
     * Generate a resume for the authenticated user
     * @param jobTitle Optional job title to tailor the resume for
     * @param format Optional resume format (chronological, functional, hybrid)
     * @param template Optional resume template (professional, creative, executive, etc.)
     * @param authentication Authentication object containing user info
     * @return Generated resume content
     */
    @PostMapping("/resume/generate")
    public ResponseEntity<AiResponseDto> generateResume(
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false, defaultValue = "chronological") String format,
            @RequestParam(required = false, defaultValue = "professional") String template,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            AiResponseDto response = aiService.generateResume(user.getId(), jobTitle, format, template);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AiResponseDto errorResponse = new AiResponseDto(
                null,
                "chronological",
                false,
                "Error generating resume: " + e.getMessage(),
                0
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Optimize resume for a specific job
     * @param jobId The job ID to optimize for
     * @param authentication Authentication object containing user info
     * @return Optimized resume content
     */
    @PostMapping("/resume/optimize/{jobId}")
    public ResponseEntity<AiResponseDto> optimizeResumeForJob(
            @PathVariable Integer jobId,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            AiResponseDto response = aiService.optimizeResumeForJob(user.getId(), jobId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AiResponseDto errorResponse = new AiResponseDto(
                null,
                "job-optimized",
                false,
                "Error optimizing resume: " + e.getMessage(),
                0
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Generate skill assessment for the authenticated user
     * @param skillName Optional specific skill to assess
     * @param authentication Authentication object containing user info
     * @return Generated skill assessment
     */
    @PostMapping("/skills/assess")
    public ResponseEntity<AiResponseDto> generateSkillAssessment(
            @RequestParam(required = false) String skillName,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            // Create a prompt for skill assessment
            String prompt = "Generate a skill assessment for user with ID " + user.getId();
            if (skillName != null && !skillName.isEmpty()) {
                prompt += " focusing on " + skillName;
            }
            AiResponseDto response = aiService.generateText(prompt, "skill-assessment");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AiResponseDto errorResponse = new AiResponseDto(
                null,
                "skill-assessment",
                false,
                "Error generating skill assessment: " + e.getMessage(),
                0
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Generate interview questions for the authenticated user
     * @param jobId Optional job ID to tailor questions for
     * @param authentication Authentication object containing user info
     * @return Generated interview questions
     */
    @PostMapping("/interview/questions")
    public ResponseEntity<AiResponseDto> generateInterviewQuestions(
            @RequestParam(required = false) Integer jobId,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            AiResponseDto response = aiService.generateInterviewQuestions(user.getId(), jobId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AiResponseDto errorResponse = new AiResponseDto(
                null,
                "interview-questions",
                false,
                "Error generating interview questions: " + e.getMessage(),
                0
            );
            return ResponseEntity.badRequest().body(errorResponse);
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