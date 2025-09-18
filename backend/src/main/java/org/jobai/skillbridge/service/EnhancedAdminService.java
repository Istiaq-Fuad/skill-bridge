package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnhancedAdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepo jobRepository;
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    @Autowired
    private AdminProfileRepository adminProfileRepository;
    
    @Autowired
    private EmployerProfileRepository employerProfileRepository;
    
    /**
     * Get platform statistics
     * @return Platform statistics
     */
    public PlatformStatistics getPlatformStatistics() {
        PlatformStatistics stats = new PlatformStatistics();
        
        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setTotalJobSeekers(userRepository.countByRole(UserRole.JOB_SEEKER));
        stats.setTotalEmployers(userRepository.countByRole(UserRole.EMPLOYER));
        stats.setTotalAdmins(userRepository.countByRole(UserRole.ADMIN));
        
        // Job statistics
        stats.setTotalJobs(jobRepository.count());
        stats.setActiveJobs(jobRepository.countByJobStatus("ACTIVE"));
        stats.setClosedJobs(jobRepository.countByJobStatus("FILLED") + jobRepository.countByJobStatus("EXPIRED"));
        
        // Application statistics
        stats.setTotalApplications(applicationRepository.count());
        stats.setPendingApplications(applicationRepository.countByStatus("APPLIED"));
        stats.setAcceptedApplications(applicationRepository.countByStatus("ACCEPTED"));
        stats.setRejectedApplications(applicationRepository.countByStatus("REJECTED"));
        
        return stats;
    }
    
    /**
     * Get all users with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of users
     */
    public List<User> getAllUsers(int page, int size) {
        return userRepository.findAll()
                .stream()
                .skip(page * size)
                .limit(size)
                .toList();
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Update user account status
     * @param userId User ID
     * @param isActive New active status
     * @return Updated user
     */
    public User updateUserAccountStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(isActive);
        return userRepository.save(user);
    }
    
    /**
     * Delete user account
     * @param userId User ID
     */
    public void deleteUserAccount(Long userId) {
        // Delete related data first
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete user's jobs if employer
        if (UserRole.EMPLOYER.equals(user.getRole())) {
            List<JobPost> jobs = jobRepository.findByEmployer(user);
            jobRepository.deleteAll(jobs);
        }
        
        // Delete user's applications if job seeker
        if (UserRole.JOB_SEEKER.equals(user.getRole())) {
            List<JobApplication> applications = applicationRepository.findByUser(user);
            applicationRepository.deleteAll(applications);
        }
        
        // Delete user's profile data
        if (UserRole.EMPLOYER.equals(user.getRole())) {
            Optional<EmployerProfile> employerProfile = employerProfileRepository.findByUser(user);
            employerProfile.ifPresent(employerProfileRepository::delete);
        } else if (UserRole.ADMIN.equals(user.getRole())) {
            Optional<AdminProfile> adminProfile = adminProfileRepository.findByUser(user);
            adminProfile.ifPresent(adminProfileRepository::delete);
        }
        
        // Delete user
        userRepository.deleteById(userId);
    }
    
    /**
     * Get all jobs with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of jobs
     */
    public List<JobPost> getAllJobs(int page, int size) {
        return jobRepository.findAll()
                .stream()
                .skip(page * size)
                .limit(size)
                .toList();
    }
    
    /**
     * Update job status
     * @param jobId Job ID
     * @param status New status
     * @return Updated job
     */
    public JobPost updateJobStatus(Long jobId, String status) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setJobStatus(status);
        return jobRepository.save(job);
    }
    
    /**
     * Delete job
     * @param jobId Job ID
     */
    public void deleteJob(Long jobId) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Delete related applications
        List<JobApplication> applications = applicationRepository.findByJobPost(job);
        applicationRepository.deleteAll(applications);
        
        // Delete job
        jobRepository.deleteById(jobId);
    }
    
    /**
     * Get all applications with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of applications
     */
    public List<JobApplication> getAllApplications(int page, int size) {
        return applicationRepository.findAll()
                .stream()
                .skip(page * size)
                .limit(size)
                .toList();
    }
    
    /**
     * Update application status
     * @param applicationId Application ID
     * @param status New status
     * @return Updated application
     */
    public JobApplication updateApplicationStatus(Long applicationId, String status) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        application.setLastUpdated(LocalDateTime.now());
        return applicationRepository.save(application);
    }
    
    /**
     * Get flagged content (placeholder for future implementation)
     * @return List of flagged content
     */
    public List<Object> getFlaggedContent() {
        // Placeholder implementation - in a real system, this would check for:
        // - Inappropriate job descriptions
        // - Offensive user profiles
        // - Spam applications
        // - Duplicate content
        return List.of();
    }
    
    /**
     * Resolve dispute (placeholder for future implementation)
     * @param disputeId Dispute ID
     * @param resolution Resolution details
     */
    public void resolveDispute(Long disputeId, String resolution) {
        // Placeholder implementation - in a real system, this would:
        // - Update dispute status
        // - Notify involved parties
        // - Log resolution details
    }
    
    // DTOs for admin data
    
    public static class PlatformStatistics {
        private long totalUsers;
        private long totalJobSeekers;
        private long totalEmployers;
        private long totalAdmins;
        private long totalJobs;
        private long activeJobs;
        private long closedJobs;
        private long totalApplications;
        private long pendingApplications;
        private long acceptedApplications;
        private long rejectedApplications;
        
        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getTotalJobSeekers() { return totalJobSeekers; }
        public void setTotalJobSeekers(long totalJobSeekers) { this.totalJobSeekers = totalJobSeekers; }
        
        public long getTotalEmployers() { return totalEmployers; }
        public void setTotalEmployers(long totalEmployers) { this.totalEmployers = totalEmployers; }
        
        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }
        
        public long getTotalJobs() { return totalJobs; }
        public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
        
        public long getActiveJobs() { return activeJobs; }
        public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }
        
        public long getClosedJobs() { return closedJobs; }
        public void setClosedJobs(long closedJobs) { this.closedJobs = closedJobs; }
        
        public long getTotalApplications() { return totalApplications; }
        public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
        
        public long getPendingApplications() { return pendingApplications; }
        public void setPendingApplications(long pendingApplications) { this.pendingApplications = pendingApplications; }
        
        public long getAcceptedApplications() { return acceptedApplications; }
        public void setAcceptedApplications(long acceptedApplications) { this.acceptedApplications = acceptedApplications; }
        
        public long getRejectedApplications() { return rejectedApplications; }
        public void setRejectedApplications(long rejectedApplications) { this.rejectedApplications = rejectedApplications; }
    }
}