package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.EnhancedAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class EnhancedAdminController {

    @Autowired
    private EnhancedAdminService enhancedAdminService;

    /**
     * Get platform statistics
     * @return Platform statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<EnhancedAdminService.PlatformStatistics> getPlatformStatistics() {
        EnhancedAdminService.PlatformStatistics stats = enhancedAdminService.getPlatformStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get all users with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<User> users = enhancedAdminService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        Optional<User> user = enhancedAdminService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update user account status
     * @param userId User ID
     * @param request Status update request
     * @return Updated user
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> updateUserAccountStatus(
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request) {
        User user = enhancedAdminService.updateUserAccountStatus(userId, request.isActive());
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user account
     * @param userId User ID
     * @return No content
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUserAccount(@PathVariable Long userId) {
        enhancedAdminService.deleteUserAccount(userId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all jobs with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of jobs
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<JobPost>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<JobPost> jobs = enhancedAdminService.getAllJobs(page, size);
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Update job status
     * @param jobId Job ID
     * @param request Status update request
     * @return Updated job
     */
    @PutMapping("/jobs/{jobId}/status")
    public ResponseEntity<JobPost> updateJobStatus(
            @PathVariable Long jobId,
            @RequestBody StatusUpdateRequest request) {
        JobPost job = enhancedAdminService.updateJobStatus(jobId, request.getStatus());
        return ResponseEntity.ok(job);
    }
    
    /**
     * Delete job
     * @param jobId Job ID
     * @return No content
     */
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        enhancedAdminService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all applications with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of applications
     */
    @GetMapping("/applications")
    public ResponseEntity<List<JobApplication>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<JobApplication> applications = enhancedAdminService.getAllApplications(page, size);
        return ResponseEntity.ok(applications);
    }
    
    /**
     * Update application status
     * @param applicationId Application ID
     * @param request Status update request
     * @return Updated application
     */
    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<JobApplication> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody StatusUpdateRequest request) {
        JobApplication application = enhancedAdminService.updateApplicationStatus(applicationId, request.getStatus());
        return ResponseEntity.ok(application);
    }
    
    /**
     * Get flagged content
     * @return List of flagged content
     */
    @GetMapping("/flagged-content")
    public ResponseEntity<List<Object>> getFlaggedContent() {
        List<Object> flaggedContent = enhancedAdminService.getFlaggedContent();
        return ResponseEntity.ok(flaggedContent);
    }
    
    /**
     * Resolve dispute
     * @param disputeId Dispute ID
     * @param request Dispute resolution request
     * @return Success response
     */
    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<String> resolveDispute(
            @PathVariable Long disputeId,
            @RequestBody DisputeResolutionRequest request) {
        enhancedAdminService.resolveDispute(disputeId, request.getResolution());
        return ResponseEntity.ok("Dispute resolved successfully");
    }
    
    /**
     * Status update request DTO
     */
    public static class StatusUpdateRequest {
        private boolean active;
        private String status;
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Dispute resolution request DTO
     */
    public static class DisputeResolutionRequest {
        private String resolution;
        
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }
}