package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.JobMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
public class JobMatchingController {
    
    @Autowired
    private JobMatchingService jobMatchingService;
    
    /**
     * Get jobs that match the authenticated user's skills
     * @param limit Maximum number of jobs to return (default: 10)
     * @param authentication Authentication object containing user info
     * @return List of matching jobs with compatibility scores
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<JobMatchingService.JobMatch>> getMatchingJobs(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            List<JobMatchingService.JobMatch> matches = jobMatchingService.findMatchingJobs(user.getId(), limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get candidates that match a specific job (employer only)
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return (default: 10)
     * @param authentication Authentication object containing user info
     * @return List of matching candidates with compatibility scores
     */
    @GetMapping("/candidates/{jobId}")
    public ResponseEntity<List<JobMatchingService.CandidateMatch>> getMatchingCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        try {
            User employer = (User) authentication.getPrincipal();
            List<JobMatchingService.CandidateMatch> matches = jobMatchingService.findMatchingCandidates(jobId, limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}