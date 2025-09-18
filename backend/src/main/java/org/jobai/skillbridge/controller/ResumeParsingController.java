package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.service.ResumeParsingService;
import org.jobai.skillbridge.service.UserService;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employer/resume")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ResumeParsingController {

    @Autowired
    private ResumeParsingService resumeParsingService;
    
    @Autowired
    private UserService userService;

    /**
     * Parse a resume file and return extracted data
     * @param file The uploaded resume file
     * @return Parsed resume data
     */
    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
        try {
            ResumeParsingService.ParsedResumeData parsedData = resumeParsingService.parseResume(file);
            return ResponseEntity.ok(parsedData);
        } catch (AiServiceException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Parsing Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred while parsing the resume"));
        }
    }

    /**
     * Parse a resume and save the extracted data to a user profile
     * @param file The uploaded resume file
     * @param userId The user ID to save the data to (optional, defaults to current user)
     * @return Success message
     */
    @PostMapping("/parse-and-save")
    public ResponseEntity<?> parseAndSaveResume(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "userId", required = false) Long userId) {
        try {
            // Get current user if userId not provided
            Long targetUserId = userId;
            if (targetUserId == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = userService.getUserByUsername(authentication.getName());
                targetUserId = currentUser.getId();
            }
            
            // Parse the resume
            ResumeParsingService.ParsedResumeData parsedData = resumeParsingService.parseResume(file);
            
            // Save to user profile
            User updatedUser = userService.updateUserProfileWithResumeData(targetUserId, parsedData);
            
            return ResponseEntity.ok(new SuccessResponse("Resume parsed and data saved successfully", parsedData));
        } catch (AiServiceException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Parsing Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred while parsing and saving the resume"));
        }
    }

    /**
     * Get skills from a parsed resume
     * @param file The uploaded resume file
     * @return List of extracted skills
     */
    @PostMapping("/skills")
    public ResponseEntity<?> extractSkills(@RequestParam("file") MultipartFile file) {
        try {
            ResumeParsingService.ParsedResumeData parsedData = resumeParsingService.parseResume(file);
            return ResponseEntity.ok(parsedData.getSkills());
        } catch (AiServiceException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Parsing Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred while extracting skills"));
        }
    }

    /**
     * Get experiences from a parsed resume
     * @param file The uploaded resume file
     * @return List of extracted experiences
     */
    @PostMapping("/experiences")
    public ResponseEntity<?> extractExperiences(@RequestParam("file") MultipartFile file) {
        try {
            ResumeParsingService.ParsedResumeData parsedData = resumeParsingService.parseResume(file);
            return ResponseEntity.ok(parsedData.getExperiences());
        } catch (AiServiceException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Parsing Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred while extracting experiences"));
        }
    }

    /**
     * Get education from a parsed resume
     * @param file The uploaded resume file
     * @return List of extracted education entries
     */
    @PostMapping("/education")
    public ResponseEntity<?> extractEducation(@RequestParam("file") MultipartFile file) {
        try {
            ResumeParsingService.ParsedResumeData parsedData = resumeParsingService.parseResume(file);
            return ResponseEntity.ok(parsedData.getEducations());
        } catch (AiServiceException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Parsing Error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal Server Error", "An unexpected error occurred while extracting education"));
        }
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Success response DTO
     */
    public static class SuccessResponse {
        private String message;
        private Object data;

        public SuccessResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}