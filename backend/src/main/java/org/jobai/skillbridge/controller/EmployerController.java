package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employers")
public class EmployerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Verify user is an employer
        if (!"EMPLOYER".equals(user.getRole().name())) {
            return ResponseEntity.status(403).build();
        }

        List<JobPost> employerJobs = jobService.getJobsByEmployerId(user.getId().intValue());

        int totalJobs = employerJobs.size();
        int activeJobs = (int) employerJobs.stream().filter(job -> true).count(); // Assuming all are active for now

        // Calculate total applications across all jobs
        int totalApplications = 0;
        int pendingApplications = 0;

        for (JobPost job : employerJobs) {
            List<JobApplication> jobApplications = applicationService.getJobApplications(job);
            totalApplications += jobApplications.size();
            pendingApplications += (int) jobApplications.stream()
                    .filter(app -> "PENDING".equals(app.getStatus()) || "APPLIED".equals(app.getStatus()))
                    .count();
        }

        // Calculate response rate (placeholder calculation)
        double responseRate = totalApplications > 0
                ? (double) (totalApplications - pendingApplications) / totalApplications * 100
                : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", totalJobs);
        stats.put("totalApplications", totalApplications);
        stats.put("pendingApplications", pendingApplications);
        stats.put("activeJobs", activeJobs);
        stats.put("responseRate", Math.round(responseRate * 100.0) / 100.0);
        stats.put("profileViews", 0); // Placeholder for now

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPost>> getEmployerJobs(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Verify user is an employer
        if (!"EMPLOYER".equals(user.getRole().name())) {
            return ResponseEntity.status(403).build();
        }

        List<JobPost> jobs = jobService.getJobsByEmployerId(user.getId().intValue());
        return ResponseEntity.ok(jobs);
    }
}