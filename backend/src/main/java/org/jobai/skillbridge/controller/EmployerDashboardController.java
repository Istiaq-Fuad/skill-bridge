package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employer/dashboard")
public class EmployerDashboardController {
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmployerDashboardStats(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        
        // Get employer's jobs
        List<JobPost> employerJobs = jobService.getJobsByEmployer(employer);
        
        // Get all applications for employer's jobs
        List<JobApplication> allApplications = applicationService.getApplicationsForEmployer(employer);
        
        // Calculate statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", employerJobs.size());
        stats.put("totalApplications", allApplications.size());
        
        // Count applications by status
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("APPLIED", 0);
        statusCounts.put("REVIEWED", 0);
        statusCounts.put("INTERVIEW", 0);
        statusCounts.put("REJECTED", 0);
        statusCounts.put("ACCEPTED", 0);
        
        for (JobApplication application : allApplications) {
            String status = application.getStatus();
            if (status != null) {
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
        }
        
        stats.put("applicationsByStatus", statusCounts);
        
        // Get recent jobs (last 5)
        stats.put("recentJobs", employerJobs.stream()
                .sorted((j1, j2) -> j2.getPostedAt().compareTo(j1.getPostedAt()))
                .limit(5)
                .toList());
        
        // Get recent applications (last 5)
        stats.put("recentApplications", allApplications.stream()
                .sorted((a1, a2) -> a2.getAppliedAt().compareTo(a1.getAppliedAt()))
                .limit(5)
                .toList());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/jobs")
    public ResponseEntity<List<JobPost>> getEmployerJobs(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        List<JobPost> employerJobs = jobService.getJobsByEmployer(employer);
        return ResponseEntity.ok(employerJobs);
    }
    
    @GetMapping("/applications")
    public ResponseEntity<List<JobApplication>> getEmployerApplications(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        List<JobApplication> applications = applicationService.getApplicationsForEmployer(employer);
        return ResponseEntity.ok(applications);
    }
}