package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    public JobService service;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<JobPost>> getAllJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company) {
        List<JobPost> jobs;
        if (search != null) {
            jobs = service.searchJobsByKeyword(search);
        } else {
            jobs = service.getJobs();
        }
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getJob(@PathVariable Integer id) {
        JobPost job = service.getJob(id);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

    @PostMapping
    public ResponseEntity<JobPost> createJob(@RequestBody JobPost jobData, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // Set the employer ID based on the authenticated user
        jobData.setEmployerId(user.getId().intValue());
        JobPost savedJob = service.saveJob(jobData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPost> updateJob(@PathVariable Integer id, @RequestBody JobPost jobData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        JobPost existingJob = service.getJob(id);

        if (existingJob == null) {
            return ResponseEntity.notFound().build();
        }

        // Only allow the job creator to update
        if (!existingJob.getEmployerId().equals(user.getId().intValue())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobData.setId(id);
        jobData.setEmployerId(user.getId().intValue());
        JobPost updatedJob = service.saveJob(jobData);
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Integer id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        JobPost existingJob = service.getJob(id);

        if (existingJob == null) {
            return ResponseEntity.notFound().build();
        }

        // Only allow the job creator to delete
        if (!existingJob.getEmployerId().equals(user.getId().intValue())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        service.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/keyword/{keyword}")
    public List<JobPost> getJobsByKeyword(@PathVariable("keyword") String keyword) {
        return service.searchJobsByKeyword(keyword);
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<List<JobPost>> getMyJobs(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Only allow employers to see their jobs
        if (!"EMPLOYER".equals(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<JobPost> jobs = service.getJobsByEmployerId(user.getId().intValue());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<JobApplication>> getJobApplications(@PathVariable Integer id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        JobPost job = service.getJob(id);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Only allow job creator to see applications
        if (!job.getEmployerId().equals(user.getId().intValue())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<JobApplication> applications = applicationService.getJobApplications(job);
        return ResponseEntity.ok(applications);
    }
}
