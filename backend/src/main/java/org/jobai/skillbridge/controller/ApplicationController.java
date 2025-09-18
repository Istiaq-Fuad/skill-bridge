package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.jobai.skillbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JobService jobService;
    
    @GetMapping
    public ResponseEntity<List<JobApplication>> getUserApplications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.getUserApplications(user));
    }
    
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<JobApplication> applyToJob(
            @PathVariable Long jobId,
            @RequestBody String coverLetter,
            @RequestParam String resumeUrl,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        JobPost jobPost = jobService.getJob(jobId);
        
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }
        
        JobApplication application = applicationService.applyToJob(user, jobPost, coverLetter, resumeUrl);
        return ResponseEntity.ok(application);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplication> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        JobApplication application = applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(application);
    }
    
    @PutMapping("/{id}/notes")
    public ResponseEntity<JobApplication> updateApplicationNotes(
            @PathVariable Long id,
            @RequestBody String notes) {
        JobApplication application = applicationService.updateApplicationNotes(id, notes);
        return ResponseEntity.ok(application);
    }
    
    @PutMapping("/{id}/feedback")
    public ResponseEntity<JobApplication> updateApplicationFeedback(
            @PathVariable Long id,
            @RequestBody String feedback) {
        JobApplication application = applicationService.updateApplicationFeedback(id, feedback);
        return ResponseEntity.ok(application);
    }
    
    @PutMapping("/{id}/schedule-interview")
    public ResponseEntity<JobApplication> scheduleInterview(
            @PathVariable Long id,
            @RequestBody LocalDateTime interviewTime) {
        JobApplication application = applicationService.scheduleInterview(id, interviewTime);
        return ResponseEntity.ok(application);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
    
    // Employer endpoint to get applications for their jobs
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getJobApplications(@PathVariable Long jobId) {
        JobPost jobPost = jobService.getJob(jobId);
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(applicationService.getJobApplications(jobPost));
    }
    
    // Employer endpoint to get applications by status
    @GetMapping("/job/{jobId}/status/{status}")
    public ResponseEntity<List<JobApplication>> getJobApplicationsByStatus(
            @PathVariable Long jobId, 
            @PathVariable String status) {
        JobPost jobPost = jobService.getJob(jobId);
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(applicationService.getJobApplicationsByStatus(jobPost, status));
    }
    
    // Employer endpoint to get all applications for their jobs
    @GetMapping("/employer")
    public ResponseEntity<List<JobApplication>> getEmployerApplications(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        return ResponseEntity.ok(applicationService.getApplicationsForEmployer(employer));
    }
}