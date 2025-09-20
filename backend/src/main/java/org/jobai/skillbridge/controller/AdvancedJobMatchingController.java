package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.service.AdvancedJobMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advanced-matching")
@CrossOrigin(origins = "${cors.allowed-origins}")
@PreAuthorize("hasRole('EMPLOYER') or hasRole('JOB_SEEKER')")
public class AdvancedJobMatchingController {

    @Autowired
    private AdvancedJobMatchingService advancedJobMatchingService;

    /**
     * Get advanced candidate matching for a job
     * 
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return (default: 10)
     * @return List of candidates with advanced compatibility scores
     */
    @GetMapping("/candidates/{jobId}")
    public ResponseEntity<?> getAdvancedMatchingCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdvancedJobMatchingService.AdvancedCandidateMatch> matches = advancedJobMatchingService
                    .findMatchingCandidates(jobId, limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Matching Error", e.getMessage()));
        }
    }

    /**
     * Get advanced job matching for a user
     * 
     * @param userId The user ID
     * @param limit  Maximum number of jobs to return (default: 10)
     * @return List of jobs with advanced compatibility scores
     */
    @GetMapping("/jobs/{userId}")
    public ResponseEntity<?> getAdvancedMatchingJobs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdvancedJobMatchingService.AdvancedJobMatch> matches = advancedJobMatchingService
                    .findMatchingJobs(userId, limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Matching Error", e.getMessage()));
        }
    }

    /**
     * Update learning models based on successful placement
     * 
     * @param request Learning update request
     * @return Success response
     */
    @PostMapping("/learning/update")
    public ResponseEntity<?> updateLearningModels(@RequestBody LearningUpdateRequest request) {
        try {
            advancedJobMatchingService.updateLearningModels(request.getJobId(), request.getUserId());
            return ResponseEntity.ok("Learning models updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Learning Update Error", e.getMessage()));
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

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Learning update request DTO
     */
    public static class LearningUpdateRequest {
        private Long jobId;
        private Long userId;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}