package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/jobs")
public class EmployerJobController {
    
    @Autowired
    private JobService jobService;
    
    @GetMapping
    public ResponseEntity<List<JobPost>> getEmployerJobs(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        List<JobPost> employerJobs = jobService.getJobsByEmployer(employer);
        return ResponseEntity.ok(employerJobs);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<JobPost>> getActiveEmployerJobs(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        List<JobPost> activeJobs = jobService.getActiveJobsByEmployer(employer);
        return ResponseEntity.ok(activeJobs);
    }
    
    @PostMapping
    public ResponseEntity<JobPost> createJob(@RequestBody JobPost jobPost, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        jobPost.setEmployer(employer);
        JobPost createdJob = jobService.addJob(jobPost);
        return ResponseEntity.ok(createdJob);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getJobById(@PathVariable Long id, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        JobPost job = jobService.getJob(id);
        
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if the job belongs to the employer
        if (!job.getEmployer().getId().equals(employer.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(job);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JobPost> updateJob(@PathVariable Long id, @RequestBody JobPost jobPost, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        JobPost existingJob = jobService.getJob(id);
        
        if (existingJob == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if the job belongs to the employer
        if (!existingJob.getEmployer().getId().equals(employer.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        jobPost.setPostId(id);
        jobPost.setEmployer(employer);
        JobPost updatedJob = jobService.updateJob(jobPost);
        return ResponseEntity.ok(updatedJob);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        JobPost existingJob = jobService.getJob(id);
        
        if (existingJob == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if the job belongs to the employer
        if (!existingJob.getEmployer().getId().equals(employer.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<JobPost> updateJobStatus(@PathVariable Long id, @RequestParam String status, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        JobPost existingJob = jobService.getJob(id);
        
        if (existingJob == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if the job belongs to the employer
        if (!existingJob.getEmployer().getId().equals(employer.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        existingJob.setJobStatus(status);
        JobPost updatedJob = jobService.updateJob(existingJob);
        return ResponseEntity.ok(updatedJob);
    }
}