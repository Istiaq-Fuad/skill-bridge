package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/job-seekers")
@CrossOrigin(origins = "http://localhost:3000")
public class JobSeekerController {

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private ResumeParsingService resumeParsingService;

    @Autowired
    private AdvancedCandidateMatchingService candidateMatchingService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TebiFileStorageService fileStorageService;

    @Autowired
    private ProfileService profileService;

    // Dashboard for job seekers
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();

            Map<String, Object> dashboard = new HashMap<>();

            // Get user applications
            List<JobApplication> applications = applicationService.getUserApplications(jobSeeker);

            // Count applications by status
            Map<String, Integer> applicationsByStatus = new HashMap<>();
            applicationsByStatus.put("APPLIED", 0);
            applicationsByStatus.put("REVIEWED", 0);
            applicationsByStatus.put("INTERVIEW", 0);
            applicationsByStatus.put("REJECTED", 0);
            applicationsByStatus.put("ACCEPTED", 0);

            for (JobApplication app : applications) {
                String status = app.getStatus() != null ? app.getStatus() : "APPLIED";
                applicationsByStatus.put(status, applicationsByStatus.getOrDefault(status, 0) + 1);
            }

            // Get profile completion percentage
            double profileCompletion = calculateProfileCompletion(jobSeeker);

            // Get job recommendations count
            List<AdvancedCandidateMatchingService.JobMatchResult> jobRecommendations = candidateMatchingService
                    .getJobRecommendations(jobSeeker, 5);

            dashboard.put("totalApplications", applications.size());
            dashboard.put("applicationsByStatus", applicationsByStatus);
            dashboard.put("profileCompletion", profileCompletion);
            dashboard.put("jobRecommendations", jobRecommendations.size());
            dashboard.put("recentApplications", applications.stream().limit(5).toList());

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Upload and parse resume
    @PostMapping("/resume/upload")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeParsingService.EnhancedParseResult> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();

            ResumeParsingService.EnhancedParseResult result = resumeParsingService.parseResumeWithStorage(file,
                    jobSeeker);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get user's resumes
    @GetMapping("/resumes")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<FileStorage>> getUserResumes(Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            List<FileStorage> resumes = resumeParsingService.getUserResumes(jobSeeker);
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Generate resume using AI
    @PostMapping("/resume/generate")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<AiResponseDto> generateResume(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();

            String jobTitle = request.get("jobTitle");
            String format = request.getOrDefault("format", "professional");
            String template = request.getOrDefault("template", "modern");

            AiResponseDto resume = mistralAiService.generateResume(
                    jobSeeker.getId(), jobTitle, format, template);

            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Optimize resume for specific job
    @PostMapping("/resume/optimize/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<AiResponseDto> optimizeResumeForJob(
            @PathVariable Integer jobId,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();

            AiResponseDto optimizedResume = mistralAiService.optimizeResumeForJob(
                    jobSeeker.getId(), jobId);

            return ResponseEntity.ok(optimizedResume);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get job recommendations
    @GetMapping("/job-recommendations")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<AdvancedCandidateMatchingService.JobMatchResult>> getJobRecommendations(
            @RequestParam(defaultValue = "20") int maxResults,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();

            List<AdvancedCandidateMatchingService.JobMatchResult> recommendations = candidateMatchingService
                    .getJobRecommendations(jobSeeker, maxResults);

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get compatibility score for specific job
    @GetMapping("/jobs/{jobId}/compatibility")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<AdvancedCandidateMatchingService.CandidateMatchResult> getJobCompatibility(
            @PathVariable Integer jobId,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            JobPost job = jobService.getJob(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            AdvancedCandidateMatchingService.CandidateMatchResult compatibility = candidateMatchingService
                    .evaluateCandidate(jobSeeker, job);

            return ResponseEntity.ok(compatibility);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get application history with analytics
    @GetMapping("/applications/history")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> getApplicationHistory(Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            List<JobApplication> applications = applicationService.getUserApplications(jobSeeker);

            Map<String, Object> history = new HashMap<>();

            // Group applications by month
            Map<String, Integer> applicationsByMonth = new HashMap<>();
            Map<String, Double> successRateByMonth = new HashMap<>();

            // Calculate success metrics
            int totalApplications = applications.size();
            long successfulApplications = applications.stream()
                    .filter(app -> "ACCEPTED".equals(app.getStatus()) || "INTERVIEW".equals(app.getStatus()))
                    .count();

            double overallSuccessRate = totalApplications > 0
                    ? (double) successfulApplications / totalApplications * 100
                    : 0;

            history.put("applications", applications);
            history.put("totalApplications", totalApplications);
            history.put("successfulApplications", successfulApplications);
            history.put("overallSuccessRate", Math.round(overallSuccessRate * 100.0) / 100.0);
            history.put("applicationsByMonth", applicationsByMonth);
            history.put("successRateByMonth", successRateByMonth);

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get skill gap analysis
    @PostMapping("/skills/gap-analysis")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Map<String, Object>> getSkillGapAnalysis(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            String targetRole = request.get("targetRole");
            String industry = request.getOrDefault("industry", "Technology");

            // Get user's current skills
            List<Skill> currentSkills = profileService.getUserSkills(jobSeeker);

            // Generate AI analysis
            String prompt = String.format(
                    "Analyze skill gaps for a job seeker transitioning to %s role in %s industry. " +
                            "Current skills: %s. " +
                            "Provide: 1) Missing critical skills, 2) Skills to improve, 3) Learning recommendations, " +
                            "4) Estimated timeline for skill development.",
                    targetRole, industry, currentSkills.stream()
                            .map(Skill::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("None specified"));

            AiResponseDto analysis = mistralAiService.generateText(prompt, "Skill Gap Analysis");

            Map<String, Object> result = new HashMap<>();
            result.put("targetRole", targetRole);
            result.put("industry", industry);
            result.put("currentSkills", currentSkills);
            result.put("analysis", analysis.getContent());
            result.put("generatedAt", new java.util.Date());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get interview preparation tips
    @PostMapping("/interview/preparation")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<AiResponseDto> getInterviewPreparation(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            Integer jobId = (Integer) request.get("jobId");
            String interviewType = (String) request.getOrDefault("interviewType", "general");

            JobPost job = null;
            if (jobId != null) {
                job = jobService.getJob(jobId);
            }

            String prompt = String.format(
                    "Generate interview preparation guide for %s %s. " +
                            "Job details: %s. " +
                            "Candidate background: %s. " +
                            "Include: 1) Common questions, 2) Technical questions (if applicable), " +
                            "3) Company research tips, 4) Questions to ask interviewer, 5) Presentation tips.",
                    interviewType,
                    job != null ? "for " + (job.getTitle() != null ? job.getTitle() : job.getPostProfile())
                            : "interview",
                    job != null ? job.getDescription() : "General position",
                    String.format("%s %s with background in %s",
                            jobSeeker.getFirstName(), jobSeeker.getLastName(),
                            jobSeeker.getBio() != null ? jobSeeker.getBio() : "various fields"));

            AiResponseDto preparation = mistralAiService.generateText(prompt, "Interview Preparation");

            return ResponseEntity.ok(preparation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Career advice and guidance
    @PostMapping("/career/advice")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<AiResponseDto> getCareerAdvice(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            String careerGoal = request.get("careerGoal");
            String currentLevel = request.getOrDefault("currentLevel", "entry");
            String timeframe = request.getOrDefault("timeframe", "1 year");

            String prompt = String.format(
                    "Provide personalized career advice for: %s %s. " +
                            "Career goal: %s. Current level: %s. Timeframe: %s. " +
                            "Background: %s. " +
                            "Include: 1) Career roadmap, 2) Skill development priorities, " +
                            "3) Networking strategies, 4) Industry insights, 5) Next steps.",
                    jobSeeker.getFirstName(), jobSeeker.getLastName(),
                    careerGoal, currentLevel, timeframe,
                    jobSeeker.getBio() != null ? jobSeeker.getBio() : "Professional seeking growth");

            AiResponseDto advice = mistralAiService.generateText(prompt, "Career Advice");

            return ResponseEntity.ok(advice);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete resume
    @DeleteMapping("/resumes/{fileId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long fileId,
            Authentication authentication) {
        try {
            User jobSeeker = (User) authentication.getPrincipal();
            resumeParsingService.deleteResume(fileId, jobSeeker);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper method to calculate profile completion
    private double calculateProfileCompletion(User user) {
        int totalFields = 10;
        int completedFields = 0;

        if (user.getFirstName() != null && !user.getFirstName().isEmpty())
            completedFields++;
        if (user.getLastName() != null && !user.getLastName().isEmpty())
            completedFields++;
        if (user.getEmail() != null && !user.getEmail().isEmpty())
            completedFields++;
        if (user.getBio() != null && !user.getBio().isEmpty())
            completedFields++;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
            completedFields++;
        if (user.getAddress() != null && !user.getAddress().isEmpty())
            completedFields++;

        // Check if user has skills, experience, education
        List<Skill> skills = profileService.getUserSkills(user);
        if (!skills.isEmpty())
            completedFields++;

        List<Experience> experience = profileService.getUserExperiences(user);
        if (!experience.isEmpty())
            completedFields++;

        List<Education> education = profileService.getUserEducations(user);
        if (!education.isEmpty())
            completedFields++;

        // Check if user has uploaded resume
        Optional<FileStorage> resume = resumeParsingService.getLatestResume(user);
        if (resume.isPresent())
            completedFields++;

        return Math.round((double) completedFields / totalFields * 100.0);
    }
}