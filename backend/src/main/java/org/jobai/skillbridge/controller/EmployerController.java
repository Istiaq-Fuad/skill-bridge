package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.JobApplication;
import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.service.ApplicationService;
import org.jobai.skillbridge.service.JobService;
import org.jobai.skillbridge.service.MistralAiService;
import org.jobai.skillbridge.service.AdvancedJobMatchingService;
import org.jobai.skillbridge.service.JobDescriptionGeneratorService;
import org.jobai.skillbridge.service.AdvancedCandidateMatchingService;
import org.jobai.skillbridge.service.ResumeParsingService;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employers")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private AdvancedJobMatchingService advancedJobMatchingService;

    @Autowired
    private JobDescriptionGeneratorService jobDescriptionGeneratorService;

    @Autowired
    private AdvancedCandidateMatchingService candidateMatchingService;

    @Autowired
    private ResumeParsingService resumeParsingService;

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

    // AI-Powered Job Performance Analysis
    @PostMapping("/jobs/{jobId}/analyze-performance")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<AiResponseDto> analyzeJobPerformance(@PathVariable Integer jobId,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            JobPost job = jobService.getJob(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify the job belongs to the authenticated employer
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(user.getId().intValue());
            boolean isOwner = employerJobs.stream().anyMatch(j -> j.getId().equals(jobId));

            if (!isOwner) {
                return ResponseEntity.status(403).build();
            }

            // Get job applications for analysis
            List<JobApplication> applications = applicationService.getJobApplications(job);

            // Build context for AI analysis
            StringBuilder context = new StringBuilder();
            context.append("Job Performance Analysis:\n");
            context.append("Job Title: ").append(job.getTitle() != null ? job.getTitle() : job.getPostProfile())
                    .append("\n");
            context.append("Total Applications: ").append(applications.size()).append("\n");

            // Application status breakdown
            Map<String, Long> statusCounts = applications.stream()
                    .collect(Collectors.groupingBy(
                            app -> app.getStatus() != null ? app.getStatus() : "UNKNOWN",
                            Collectors.counting()));
            context.append("Application Status Breakdown: ").append(statusCounts).append("\n");

            String prompt = context.toString() +
                    "\nAnalyze this job posting performance and provide insights on:\n" +
                    "1. Application volume assessment\n" +
                    "2. Candidate engagement patterns\n" +
                    "3. Job posting effectiveness\n" +
                    "4. Recommendations for improvement\n" +
                    "5. Competitive positioning suggestions\n" +
                    "Provide actionable recommendations to improve job performance.";

            return ResponseEntity.ok(mistralAiService.generateText("job_performance", prompt));
        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // AI-Powered Candidate Recommendations
    @GetMapping("/jobs/{jobId}/recommended-candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String, Object>> getRecommendedCandidates(@PathVariable Integer jobId,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            JobPost job = jobService.getJob(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify job ownership
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(user.getId().intValue());
            boolean isOwner = employerJobs.stream().anyMatch(j -> j.getId().equals(jobId));

            if (!isOwner) {
                return ResponseEntity.status(403).build();
            }

            // Use advanced matching service to find candidates
            var candidateMatches = advancedJobMatchingService.findMatchingCandidates(jobId.longValue(), 10);
            List<Map<String, Object>> candidates = candidateMatches.stream()
                    .map(match -> {
                        Map<String, Object> candidateMap = new HashMap<>();
                        candidateMap.put("userId", match.getCandidate().getId());
                        candidateMap.put("username", match.getCandidate().getUsername());
                        candidateMap.put("email", match.getCandidate().getEmail());
                        candidateMap.put("compatibilityScore", match.getCompatibilityScore());
                        candidateMap.put("skillMatchScore", match.getSkillMatchScore());
                        candidateMap.put("experienceMatchScore", match.getExperienceMatchScore());
                        candidateMap.put("locationMatchScore", match.getLocationMatchScore());
                        return candidateMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("jobTitle", job.getTitle() != null ? job.getTitle() : job.getPostProfile());
            response.put("recommendedCandidates", candidates);
            response.put("totalRecommendations", candidates.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // AI-Powered Application Insights
    @PostMapping("/analytics/application-insights")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<AiResponseDto> generateApplicationInsights(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(user.getId().intValue());

            // Gather application data across all jobs
            Map<String, Object> analyticsData = new HashMap<>();
            int totalApplications = 0;
            Map<String, Integer> applicationsByJob = new HashMap<>();
            Map<String, Integer> applicationsByStatus = new HashMap<>();

            for (JobPost job : employerJobs) {
                List<JobApplication> applications = applicationService.getJobApplications(job);
                String jobTitle = job.getTitle() != null ? job.getTitle() : job.getPostProfile();
                applicationsByJob.put(jobTitle, applications.size());
                totalApplications += applications.size();

                // Count by status
                for (JobApplication app : applications) {
                    String status = app.getStatus() != null ? app.getStatus() : "UNKNOWN";
                    applicationsByStatus.put(status, applicationsByStatus.getOrDefault(status, 0) + 1);
                }
            }

            analyticsData.put("totalJobs", employerJobs.size());
            analyticsData.put("totalApplications", totalApplications);
            analyticsData.put("applicationsByJob", applicationsByJob);
            analyticsData.put("applicationsByStatus", applicationsByStatus);

            String prompt = "Employer Application Analytics Summary:\n" + analyticsData.toString() +
                    "\nProvide insights on:\n" +
                    "1. Hiring funnel performance\n" +
                    "2. Job posting effectiveness comparison\n" +
                    "3. Candidate quality assessment\n" +
                    "4. Time-to-hire optimization\n" +
                    "5. Recruitment strategy recommendations\n" +
                    "Focus on actionable insights to improve hiring outcomes.";

            return ResponseEntity.ok(mistralAiService.generateText("employer_insights", prompt));
        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Enhanced Dashboard with AI Recommendations
    @GetMapping("/dashboard-enhanced")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String, Object>> getEnhancedDashboard(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(user.getId().intValue());

            Map<String, Object> dashboard = new HashMap<>();

            // Basic stats
            int totalJobs = employerJobs.size();
            int totalApplications = 0;
            int pendingApplications = 0;

            for (JobPost job : employerJobs) {
                List<JobApplication> jobApplications = applicationService.getJobApplications(job);
                totalApplications += jobApplications.size();
                pendingApplications += (int) jobApplications.stream()
                        .filter(app -> "PENDING".equals(app.getStatus()) || "APPLIED".equals(app.getStatus()))
                        .count();
            }

            dashboard.put("totalJobs", totalJobs);
            dashboard.put("totalApplications", totalApplications);
            dashboard.put("pendingApplications", pendingApplications);

            // Top performing jobs
            List<Map<String, Object>> topJobs = employerJobs.stream()
                    .map(job -> {
                        List<JobApplication> apps = applicationService.getJobApplications(job);
                        Map<String, Object> jobStats = new HashMap<>();
                        jobStats.put("jobId", job.getId());
                        jobStats.put("title", job.getTitle() != null ? job.getTitle() : job.getPostProfile());
                        jobStats.put("applications", apps.size());
                        return jobStats;
                    })
                    .sorted((a, b) -> Integer.compare((Integer) b.get("applications"), (Integer) a.get("applications")))
                    .limit(5)
                    .collect(Collectors.toList());

            dashboard.put("topPerformingJobs", topJobs);

            // AI recommendations summary
            if (totalJobs > 0) {
                String prompt = "Employer Dashboard Summary: " + totalJobs + " jobs, " + totalApplications +
                        " total applications, " + pendingApplications + " pending applications.\n" +
                        "Provide 3 quick actionable recommendations to improve hiring performance.";

                AiResponseDto aiRecommendations = mistralAiService.generateText("dashboard_tips", prompt);
                dashboard.put("aiRecommendations", aiRecommendations.getContent());
            }

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Generate Job Description using AI
    @PostMapping("/generate-job-description")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDescriptionGeneratorService.JobDescriptionResult> generateJobDescription(
            @RequestBody JobDescriptionGeneratorService.JobDescriptionRequest request,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            JobDescriptionGeneratorService.JobDescriptionResult result = jobDescriptionGeneratorService
                    .generateJobDescription(request, employer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Enhance existing job description
    @PostMapping("/enhance-job-description")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDescriptionGeneratorService.JobDescriptionResult> enhanceJobDescription(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            String existingDescription = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> improvements = (List<String>) request.get("improvements");

            JobDescriptionGeneratorService.JobDescriptionResult result = jobDescriptionGeneratorService
                    .enhanceJobDescription(existingDescription, improvements, employer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Find best candidates for a job
    @GetMapping("/jobs/{jobId}/candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<AdvancedCandidateMatchingService.CandidateMatchResult>> findCandidates(
            @PathVariable Integer jobId,
            @RequestParam(defaultValue = "10") int maxResults,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            JobPost jobPost = jobService.getJob(jobId);

            if (jobPost == null || !jobPost.getEmployerId().equals(employer.getId().intValue())) {
                return ResponseEntity.notFound().build();
            }

            List<AdvancedCandidateMatchingService.CandidateMatchResult> candidates = candidateMatchingService
                    .findBestCandidates(jobPost, maxResults);
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Evaluate a specific candidate for a job
    @GetMapping("/jobs/{jobId}/candidates/{candidateId}/evaluate")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<AdvancedCandidateMatchingService.CandidateMatchResult> evaluateCandidate(
            @PathVariable Integer jobId,
            @PathVariable Long candidateId,
            Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            JobPost jobPost = jobService.getJob(jobId);

            if (jobPost == null || !jobPost.getEmployerId().equals(employer.getId().intValue())) {
                return ResponseEntity.notFound().build();
            }

            // Get candidate - you'd need to inject UserService for this
            // User candidate = userService.getUserById(candidateId);
            // if (candidate == null) {
            // return ResponseEntity.notFound().build();
            // }

            // AdvancedCandidateMatchingService.CandidateMatchResult result =
            // candidateMatchingService.evaluateCandidate(candidate, jobPost);
            // return ResponseEntity.ok(result);

            // Temporary placeholder until UserService is injected
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get hiring analytics
    @GetMapping("/analytics/hiring")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String, Object>> getHiringAnalytics(Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(employer.getId().intValue());

            Map<String, Object> analytics = new HashMap<>();
            Map<String, Integer> applicationsByMonth = new HashMap<>();
            Map<String, Double> avgTimeToHire = new HashMap<>();
            Map<String, Integer> hiresBySkill = new HashMap<>();

            for (JobPost job : employerJobs) {
                List<JobApplication> applications = applicationService.getJobApplications(job);
                // Calculate monthly application statistics
                // This would need more detailed implementation based on your requirements
            }

            analytics.put("applicationsByMonth", applicationsByMonth);
            analytics.put("avgTimeToHire", avgTimeToHire);
            analytics.put("hiresBySkill", hiresBySkill);
            analytics.put("totalJobs", employerJobs.size());

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get candidate pipeline for all jobs
    @GetMapping("/pipeline")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String, Object>> getCandidatePipeline(Authentication authentication) {
        try {
            User employer = (User) authentication.getPrincipal();
            List<JobPost> employerJobs = jobService.getJobsByEmployerId(employer.getId().intValue());

            Map<String, Object> pipeline = new HashMap<>();
            Map<String, Integer> statusCounts = new HashMap<>();
            List<Map<String, Object>> recentApplications = new ArrayList<>();

            statusCounts.put("APPLIED", 0);
            statusCounts.put("REVIEWED", 0);
            statusCounts.put("INTERVIEW", 0);
            statusCounts.put("REJECTED", 0);
            statusCounts.put("ACCEPTED", 0);

            for (JobPost job : employerJobs) {
                List<JobApplication> applications = applicationService.getJobApplications(job);

                for (JobApplication app : applications) {
                    String status = app.getStatus() != null ? app.getStatus() : "APPLIED";
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);

                    // Add to recent applications (last 10)
                    if (recentApplications.size() < 10) {
                        Map<String, Object> appInfo = new HashMap<>();
                        appInfo.put("id", app.getId());
                        appInfo.put("jobTitle", job.getTitle() != null ? job.getTitle() : job.getPostProfile());
                        appInfo.put("candidateName",
                                app.getUser() != null ? app.getUser().getFirstName() + " " + app.getUser().getLastName()
                                        : "Unknown");
                        appInfo.put("status", status);
                        appInfo.put("appliedAt", app.getAppliedAt());
                        recentApplications.add(appInfo);
                    }
                }
            }

            pipeline.put("statusCounts", statusCounts);
            pipeline.put("recentApplications", recentApplications);

            return ResponseEntity.ok(pipeline);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}