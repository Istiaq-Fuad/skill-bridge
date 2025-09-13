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
            @PathVariable Integer jobId,
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
    
    // Employer endpoint to get applications for their jobs
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getJobApplications(@PathVariable Integer jobId) {
        JobPost jobPost = jobService.getJob(jobId);
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(applicationService.getJobApplications(jobPost));
    }
}