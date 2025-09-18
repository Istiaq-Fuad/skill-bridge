package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.EnhancedEmployerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/enhanced-dashboard")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class EnhancedEmployerDashboardController {

    @Autowired
    private EnhancedEmployerDashboardService enhancedEmployerDashboardService;

    /**
     * Get enhanced dashboard statistics for the employer
     * @param authentication Authentication object containing the employer user
     * @return Enhanced dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getEnhancedDashboardStats(Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            EnhancedEmployerDashboardService.EnhancedDashboardStats stats = 
                    enhancedEmployerDashboardService.getEnhancedDashboardStats(employer);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Dashboard Error", e.getMessage()));
        }
    }
    
    /**
     * Get detailed analytics for a specific job
     * @param jobId The job ID
     * @param authentication Authentication object containing the employer user
     * @return Job analytics
     */
    @GetMapping("/jobs/{jobId}/analytics")
    public ResponseEntity<?> getJobAnalytics(@PathVariable Long jobId, Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            EnhancedEmployerDashboardService.JobAnalytics analytics = 
                    enhancedEmployerDashboardService.getJobAnalytics(jobId, employer);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Analytics Error", e.getMessage()));
        }
    }
    
    /**
     * Bulk update job statuses
     * @param request Bulk update request containing job IDs and new status
     * @param authentication Authentication object containing the employer user
     * @return Number of jobs updated
     */
    @PutMapping("/jobs/bulk-update")
    public ResponseEntity<?> bulkUpdateJobStatuses(@RequestBody BulkUpdateRequest request, Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            int updatedCount = enhancedEmployerDashboardService.bulkUpdateJobStatuses(
                    request.getJobIds(), request.getStatus(), employer);
            return ResponseEntity.ok(new BulkUpdateResponse(updatedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Bulk Update Error", e.getMessage()));
        }
    }
    
    /**
     * Get enhanced candidate information for a job
     * @param jobId The job ID
     * @param authentication Authentication object containing the employer user
     * @return List of enhanced candidate information
     */
    @GetMapping("/jobs/{jobId}/candidates")
    public ResponseEntity<?> getEnhancedCandidates(@PathVariable Long jobId, Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            List<EnhancedEmployerDashboardService.EnhancedCandidateInfo> candidates = 
                    enhancedEmployerDashboardService.getEnhancedCandidatesForJob(jobId, employer);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Candidates Error", e.getMessage()));
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
     * Bulk update request DTO
     */
    public static class BulkUpdateRequest {
        private List<Long> jobIds;
        private String status;

        public List<Long> getJobIds() { return jobIds; }
        public void setJobIds(List<Long> jobIds) { this.jobIds = jobIds; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Bulk update response DTO
     */
    public static class BulkUpdateResponse {
        private int updatedCount;

        public BulkUpdateResponse(int updatedCount) {
            this.updatedCount = updatedCount;
        }

        public int getUpdatedCount() { return updatedCount; }
        public void setUpdatedCount(int updatedCount) { this.updatedCount = updatedCount; }
    }
}