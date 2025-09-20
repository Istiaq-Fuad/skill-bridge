package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.IntelligentJobDescriptionService;
import org.jobai.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligent-jobs")
@CrossOrigin(origins = "${cors.allowed-origins}")
@PreAuthorize("hasRole('EMPLOYER')")
public class IntelligentJobDescriptionController {

    @Autowired
    private IntelligentJobDescriptionService intelligentJobDescriptionService;

    @Autowired
    private JobService jobService;

    /**
     * Generate an optimized job description based on minimal input
     * 
     * @param request        Job description generation request
     * @param authentication Authentication object
     * @return Generated job description
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateJobDescription(
            @RequestBody JobDescriptionGenerationRequest request,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();

            IntelligentJobDescriptionService.JobDescriptionGenerationResult result = intelligentJobDescriptionService
                    .generateJobDescription(
                            request.getJobTitle(),
                            request.getIndustry(),
                            request.getExperienceLevel(),
                            request.getLocation());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Job Description Generation Error", e.getMessage()));
        }
    }

    /**
     * Optimize an existing job description
     * 
     * @param jobId          The job ID to optimize
     * @param authentication Authentication object
     * @return Optimized job description
     */
    @PostMapping("/optimize/{jobId}")
    public ResponseEntity<?> optimizeJobDescription(
            @PathVariable Integer jobId,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();

            // Get the job to optimize
            JobPost job = jobService.getJob(jobId);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if the job belongs to the employer
            if (!job.getEmployerId().equals(employer.getId().intValue())) {
                return ResponseEntity.status(403).build();
            }

            IntelligentJobDescriptionService.JobDescriptionOptimizationResult result = intelligentJobDescriptionService
                    .optimizeJobDescription(job);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Job Description Optimization Error", e.getMessage()));
        }
    }

    /**
     * Suggest relevant skills for a job
     * 
     * @param request        Skill suggestion request
     * @param authentication Authentication object
     * @return Suggested skills
     */
    @PostMapping("/skills/suggest")
    public ResponseEntity<?> suggestSkills(
            @RequestBody SkillSuggestionRequest request,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();

            IntelligentJobDescriptionService.SkillSuggestionResult result = intelligentJobDescriptionService
                    .suggestSkills(
                            request.getJobTitle(),
                            request.getIndustry(),
                            request.getExperienceLevel());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Skill Suggestion Error", e.getMessage()));
        }
    }

    /**
     * Suggest competitive salary ranges for a job
     * 
     * @param request        Salary suggestion request
     * @param authentication Authentication object
     * @return Suggested salary ranges
     */
    @PostMapping("/salary/suggest")
    public ResponseEntity<?> suggestSalaryRanges(
            @RequestBody SalarySuggestionRequest request,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();

            IntelligentJobDescriptionService.SalarySuggestionResult result = intelligentJobDescriptionService
                    .suggestSalaryRanges(
                            request.getJobTitle(),
                            request.getIndustry(),
                            request.getExperienceLevel(),
                            request.getLocation());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Salary Suggestion Error", e.getMessage()));
        }
    }

    /**
     * Get all employer's jobs with intelligent insights
     * 
     * @param authentication Authentication object
     * @return List of jobs with insights
     */
    @GetMapping("/employer/jobs")
    public ResponseEntity<?> getEmployerJobsWithInsights(Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(employer.getId().intValue());

            // In a real implementation, we would add intelligent insights to each job
            // For now, we'll just return the jobs
            return ResponseEntity.ok(employerJobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Job Retrieval Error", e.getMessage()));
        }
    }

    /**
     * Apply intelligent enhancements to a job post
     * 
     * @param jobId          The job ID
     * @param enhancements   The enhancements to apply
     * @param authentication Authentication object
     * @return Updated job post
     */
    @PutMapping("/enhance/{jobId}")
    public ResponseEntity<?> enhanceJobPost(
            @PathVariable Integer jobId,
            @RequestBody JobEnhancementRequest enhancements,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();

            // Get the job to enhance
            JobPost job = jobService.getJob(jobId);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if the job belongs to the employer
            if (!job.getEmployerId().equals(employer.getId().intValue())) {
                return ResponseEntity.status(403).build();
            }

            // Apply the enhancements
            if (enhancements.getTitle() != null) {
                job.setTitle(enhancements.getTitle());
            }

            if (enhancements.getDescription() != null) {
                job.setDescription(enhancements.getDescription());
            }

            if (enhancements.getTechStack() != null) {
                job.setPostTechStack(enhancements.getTechStack());
            }

            if (enhancements.getLocation() != null) {
                job.setLocation(enhancements.getLocation());
            }

            if (enhancements.getSalaryMin() != null) {
                job.setSalary(enhancements.getSalaryMin().intValue());
            }

            // Update the job
            jobService.updateJob(job);

            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Job Enhancement Error", e.getMessage()));
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

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Job description generation request DTO
     */
    public static class JobDescriptionGenerationRequest {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private String location;

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public void setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    /**
     * Skill suggestion request DTO
     */
    public static class SkillSuggestionRequest {
        private String jobTitle;
        private String industry;
        private String experienceLevel;

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public void setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
        }
    }

    /**
     * Salary suggestion request DTO
     */
    public static class SalarySuggestionRequest {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private String location;

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public void setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    /**
     * Job enhancement request DTO
     */
    public static class JobEnhancementRequest {
        private String title;
        private String description;
        private List<String> techStack;
        private String location;
        private String employmentType;
        private Double salaryMin;
        private Double salaryMax;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTechStack() {
            return techStack;
        }

        public void setTechStack(List<String> techStack) {
            this.techStack = techStack;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getEmploymentType() {
            return employmentType;
        }

        public void setEmploymentType(String employmentType) {
            this.employmentType = employmentType;
        }

        public Double getSalaryMin() {
            return salaryMin;
        }

        public void setSalaryMin(Double salaryMin) {
            this.salaryMin = salaryMin;
        }

        public Double getSalaryMax() {
            return salaryMax;
        }

        public void setSalaryMax(Double salaryMax) {
            this.salaryMax = salaryMax;
        }
    }
}