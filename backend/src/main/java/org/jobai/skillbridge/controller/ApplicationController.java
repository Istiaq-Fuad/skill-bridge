package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.jobai.skillbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping
    public ResponseEntity<JobApplication> applyForJob(@RequestBody Map<String, Integer> request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Integer jobId = request.get("jobId");

        if (jobId == null) {
            return ResponseEntity.badRequest().build();
        }

        JobPost jobPost = jobService.getJob(jobId);
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }

        JobApplication application = applicationService.applyToJob(user, jobPost, "", "");
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplication> updateApplicationStatus(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        JobApplication application = applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(application);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    // Frontend expected endpoints
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobApplication>> getUserApplications(@PathVariable Long userId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        // Users can only see their own applications, or employers can see applications
        // to their jobs
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(applicationService.getUserApplications(currentUser));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getJobApplications(@PathVariable Integer jobId) {
        JobPost jobPost = jobService.getJob(jobId);
        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(applicationService.getJobApplications(jobPost));
    }

    @GetMapping("/job/{jobId}/detailed")
    public ResponseEntity<List<JobApplication>> getJobApplicationsWithDetails(@PathVariable Integer jobId,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        JobPost jobPost = jobService.getJob(jobId);

        if (jobPost == null) {
            return ResponseEntity.notFound().build();
        }

        // Only allow employers to see detailed applications for their own jobs
        if (!"EMPLOYER".equals(currentUser.getRole().name()) ||
                !jobPost.getEmployerId().equals(currentUser.getId().intValue())) {
            return ResponseEntity.status(403).build();
        }

        List<JobApplication> applications = applicationService.getJobApplications(jobPost);
        // The applications should already include user details from the database
        // relationships
        return ResponseEntity.ok(applications);
    }
}