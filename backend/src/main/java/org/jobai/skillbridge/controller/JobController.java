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
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    public JobService service;

    @GetMapping("/")
    public String home() {
        return "Welcome to the Job Portal";
    }

    @GetMapping
    public List<JobPost> getAllJobs() {
        return service.getJobs();
    }

    @GetMapping("/keyword/{keyword}")
    public List<JobPost> getJobsByKeyword(@PathVariable("keyword") String keyword) {
        return service.searchJobsByKeyword(keyword);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getJobById(@PathVariable Long id) {
        JobPost job = service.getJob(id);
        if (job != null) {
            return ResponseEntity.ok(job);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Employer endpoints for job management
    @PostMapping
    public ResponseEntity<JobPost> createJob(@RequestBody JobPost jobPost, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        jobPost.setEmployer(employer);
        JobPost createdJob = service.addJob(jobPost);
        return ResponseEntity.ok(createdJob);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JobPost> updateJob(@PathVariable Long id, @RequestBody JobPost jobPost, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        jobPost.setPostId(id);
        jobPost.setEmployer(employer);
        JobPost updatedJob = service.updateJob(jobPost);
        return ResponseEntity.ok(updatedJob);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        service.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobPost>> getMyJobs(Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        List<JobPost> jobs = service.getJobsByEmployer(employer);
        return ResponseEntity.ok(jobs);
    }
}
