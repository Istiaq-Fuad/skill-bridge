package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.service.EnhancedJobMatchingService;
import org.jobai.skillbridge.service.JobMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/matching")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class EnhancedJobMatchingController {

    @Autowired
    private EnhancedJobMatchingService enhancedJobMatchingService;
    
    @Autowired
    private JobMatchingService jobMatchingService;

    /**
     * Get enhanced candidate matching for a job with detailed scoring
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return (default: 10)
     * @return List of candidates with detailed compatibility scores
     */
    @GetMapping("/candidates/{jobId}")
    public ResponseEntity<?> getMatchingCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<EnhancedJobMatchingService.DetailedCandidateMatch> matches = 
                    enhancedJobMatchingService.findMatchingCandidates(jobId, limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Matching Error", e.getMessage()));
        }
    }
    
    /**
     * Get traditional candidate matching for comparison
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return (default: 10)
     * @return List of candidates with basic compatibility scores
     */
    @GetMapping("/candidates/{jobId}/traditional")
    public ResponseEntity<?> getTraditionalMatchingCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<JobMatchingService.CandidateMatch> matches = 
                    jobMatchingService.findMatchingCandidates(jobId, limit);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Matching Error", e.getMessage()));
        }
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}