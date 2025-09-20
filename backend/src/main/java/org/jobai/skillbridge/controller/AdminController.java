package org.jobai.skillbridge.controller;

import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.jobai.skillbridge.service.*;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Job Management
    @GetMapping("/jobs")
    public ResponseEntity<List<JobPost>> getAllJobs() {
        List<JobPost> jobs = jobService.getJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobPost> getJobById(@PathVariable Integer id) {
        JobPost job = jobService.getJob(id);
        if (job != null) {
            return ResponseEntity.ok(job);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Integer id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Enhanced Analytics Dashboard
    @GetMapping("/analytics/overview")
    public ResponseEntity<Map<String, Object>> getAnalyticsOverview() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            // Basic counts
            List<User> allUsers = userService.getAllUsers();
            List<JobPost> allJobs = jobService.getJobs();

            analytics.put("totalUsers", allUsers.size());
            analytics.put("totalJobs", allJobs.size());

            // User distribution by role
            Map<String, Long> usersByRole = new HashMap<>();
            allUsers.forEach(user -> {
                String role = user.getRole().name();
                usersByRole.put(role, usersByRole.getOrDefault(role, 0L) + 1);
            });
            analytics.put("usersByRole", usersByRole);

            // Job statistics by employer
            Map<String, Long> jobsByEmployer = new HashMap<>();
            allJobs.forEach(job -> {
                String employerName = job.getCompany() != null ? job.getCompany() : "Unknown";
                jobsByEmployer.put(employerName, jobsByEmployer.getOrDefault(employerName, 0L) + 1);
            });
            analytics.put("jobsByEmployer", jobsByEmployer);

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Comprehensive Admin Dashboard
    @GetMapping("/dashboard/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();

            // Enhanced User Statistics
            List<User> allUsers = userRepository.findAll();
            Map<String, Long> usersByRole = allUsers.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            user -> user.getRole().name(),
                            java.util.stream.Collectors.counting()));

            // Job Application Analytics
            List<JobApplication> allApplications = jobApplicationRepository.findAll();
            Map<String, Long> applicationsByStatus = allApplications.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            app -> app.getStatus() != null ? app.getStatus() : "PENDING",
                            java.util.stream.Collectors.counting()));

            // File Storage Statistics
            List<FileStorage> allFiles = fileStorageRepository.findAll();
            Map<String, Long> filesByCategory = allFiles.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            file -> file.getCategory().name(),
                            java.util.stream.Collectors.counting()));

            // Calculate success rate
            long totalApplications = allApplications.size();
            long successfulApplications = allApplications.stream()
                    .filter(app -> "ACCEPTED".equals(app.getStatus()))
                    .count();
            double successRate = totalApplications > 0 ? (double) successfulApplications / totalApplications * 100 : 0;

            // Active users count
            long activeUsers = allUsers.stream()
                    .filter(User::isActive)
                    .count();

            dashboard.put("totalUsers", allUsers.size());
            dashboard.put("activeUsers", activeUsers);
            dashboard.put("usersByRole", usersByRole);
            dashboard.put("totalJobs", jobService.getJobs().size());
            dashboard.put("totalApplications", totalApplications);
            dashboard.put("applicationsByStatus", applicationsByStatus);
            dashboard.put("successRate", Math.round(successRate * 100.0) / 100.0);
            dashboard.put("filesByCategory", filesByCategory);
            dashboard.put("totalFiles", allFiles.size());
            dashboard.put("platformHealth", determineHealthStatus(activeUsers, totalApplications, successRate));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // System Performance Analytics
    @GetMapping("/analytics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceAnalytics() {
        try {
            Map<String, Object> performance = new HashMap<>();

            // Memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            Map<String, Object> memoryStats = new HashMap<>();
            memoryStats.put("maxMemoryMB", maxMemory / 1024 / 1024);
            memoryStats.put("totalMemoryMB", totalMemory / 1024 / 1024);
            memoryStats.put("usedMemoryMB", usedMemory / 1024 / 1024);
            memoryStats.put("freeMemoryMB", freeMemory / 1024 / 1024);
            memoryStats.put("memoryUsagePercent", Math.round((double) usedMemory / totalMemory * 100));

            // Database health checks
            boolean databaseHealthy = true;
            try {
                userRepository.count();
                jobApplicationRepository.count();
                fileStorageRepository.count();
            } catch (Exception e) {
                databaseHealthy = false;
            }

            performance.put("memoryStats", memoryStats);
            performance.put("databaseHealthy", databaseHealthy);
            performance.put("timestamp", LocalDateTime.now());
            performance.put("overallStatus", databaseHealthy ? "HEALTHY" : "DEGRADED");

            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // User Activity Analytics
    @GetMapping("/analytics/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivityAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            List<User> allUsers = userRepository.findAll();
            List<JobApplication> allApplications = jobApplicationRepository.findAll();
            List<FileStorage> allFiles = fileStorageRepository.findAll();

            // Active users by role
            long activeJobSeekers = allUsers.stream()
                    .filter(user -> user.getRole() == UserRole.JOB_SEEKER && user.isActive())
                    .count();

            long activeEmployers = allUsers.stream()
                    .filter(user -> user.getRole() == UserRole.EMPLOYER && user.isActive())
                    .count();

            // File upload activity
            Map<String, Long> fileUploadsbyCategory = allFiles.stream()
                    .filter(FileStorage::isActive)
                    .collect(java.util.stream.Collectors.groupingBy(
                            file -> file.getCategory().name(),
                            java.util.stream.Collectors.counting()));

            // Application activity by user role
            Map<String, Long> applicationsPerRole = new HashMap<>();
            for (JobApplication app : allApplications) {
                if (app.getUser() != null) {
                    String role = app.getUser().getRole().name();
                    applicationsPerRole.put(role, applicationsPerRole.getOrDefault(role, 0L) + 1);
                }
            }

            analytics.put("activeJobSeekers", activeJobSeekers);
            analytics.put("activeEmployers", activeEmployers);
            analytics.put("fileUploadsbyCategory", fileUploadsbyCategory);
            analytics.put("totalFileUploads", allFiles.size());
            analytics.put("applicationsPerRole", applicationsPerRole);
            analytics.put("totalActiveUsers", activeJobSeekers + activeEmployers);

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper method to determine platform health
    private String determineHealthStatus(long activeUsers, long totalApplications, double successRate) {
        if (activeUsers > 100 && totalApplications > 50 && successRate > 20) {
            return "Excellent";
        } else if (activeUsers > 50 && totalApplications > 20 && successRate > 10) {
            return "Good";
        } else if (activeUsers > 10 && totalApplications > 5) {
            return "Fair";
        } else {
            return "Needs Attention";
        }
    }

    // AI-Powered Insights
    @PostMapping("/analytics/insights")
    public ResponseEntity<AiResponseDto> generateInsights(@RequestBody Map<String, Object> analyticsData) {
        try {
            // Create context for AI analysis
            StringBuilder context = new StringBuilder();
            context.append("Platform Analytics Summary:\n");

            if (analyticsData.containsKey("totalUsers")) {
                context.append("Total Users: ").append(analyticsData.get("totalUsers")).append("\n");
            }
            if (analyticsData.containsKey("totalJobs")) {
                context.append("Total Jobs: ").append(analyticsData.get("totalJobs")).append("\n");
            }

            // Add user distribution
            if (analyticsData.containsKey("usersByRole")) {
                context.append("User Distribution: ").append(analyticsData.get("usersByRole")).append("\n");
            }

            // Add job statistics
            if (analyticsData.containsKey("jobsByEmployer")) {
                context.append("Job Distribution by Employer: ").append(analyticsData.get("jobsByEmployer"))
                        .append("\n");
            }

            String prompt = context.toString() +
                    "\nAs an AI analyst, provide insights on:\n" +
                    "1. Platform growth trends\n" +
                    "2. User engagement patterns\n" +
                    "3. Job market dynamics\n" +
                    "4. Recommendations for improvement\n" +
                    "5. Potential areas of concern\n" +
                    "Keep the analysis concise but actionable.";

            AiResponseDto response = mistralAiService.generateText("admin_insights", prompt);

            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analytics/recommendations")
    public ResponseEntity<AiResponseDto> generateRecommendations() {
        try {
            // Gather current platform statistics
            List<User> allUsers = userService.getAllUsers();
            List<JobPost> allJobs = jobService.getJobs();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("totalJobs", allJobs.size());

            // User distribution by role
            Map<String, Long> usersByRole = new HashMap<>();
            allUsers.forEach(user -> {
                String role = user.getRole().name();
                usersByRole.put(role, usersByRole.getOrDefault(role, 0L) + 1);
            });
            stats.put("usersByRole", usersByRole);

            String prompt = "Based on the following job platform statistics: " + stats.toString() +
                    "\nGenerate strategic recommendations for:\n" +
                    "1. Improving user acquisition and retention\n" +
                    "2. Enhancing job posting quality and relevance\n" +
                    "3. Increasing application success rates\n" +
                    "4. Platform optimization opportunities\n" +
                    "5. Revenue growth strategies\n" +
                    "Provide specific, actionable recommendations with expected impact.";

            AiResponseDto response = mistralAiService.generateText("admin_recommendations", prompt);

            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Content Moderation
    @PostMapping("/moderate/job/{jobId}")
    public ResponseEntity<AiResponseDto> moderateJobContent(@PathVariable Integer jobId) {
        try {
            JobPost job = jobService.getJob(jobId);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            String content = "";
            if (job.getTitle() != null)
                content += job.getTitle() + "\n";
            if (job.getDescription() != null)
                content += job.getDescription() + "\n";
            if (job.getPostProfile() != null)
                content += job.getPostProfile() + "\n";
            if (job.getPostDesc() != null)
                content += job.getPostDesc() + "\n";

            String prompt = "Analyze the following job posting for content moderation:\n" + content +
                    "\nCheck for:\n" +
                    "1. Inappropriate or discriminatory language\n" +
                    "2. Misleading information\n" +
                    "3. Spam or low-quality content\n" +
                    "4. Policy violations\n" +
                    "5. Professional standards compliance\n" +
                    "Provide a moderation assessment with specific recommendations.";

            AiResponseDto response = mistralAiService.generateText("content_moderation", prompt);

            return ResponseEntity.ok(response);
        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Platform Health Check
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();

            List<User> allUsers = userService.getAllUsers();
            List<JobPost> allJobs = jobService.getJobs();

            // System metrics
            health.put("timestamp", LocalDateTime.now());
            health.put("totalUsers", allUsers.size());
            health.put("totalJobs", allJobs.size());

            // Calculate health scores based on available data
            double activityScore = Math.min(100, allUsers.size() * 2.0 + allJobs.size() * 5.0);
            health.put("activityScore", activityScore);

            // Overall health status
            String status = "healthy";
            if (activityScore < 30) {
                status = "needs_attention";
            } else if (activityScore < 60) {
                status = "moderate";
            }
            health.put("overallStatus", status);

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Advanced User Management
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            boolean isActive = request.getOrDefault("isActive", true);
            user.setActive(isActive);

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get detailed user information with related data
    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            Map<String, Object> profile = new HashMap<>();

            profile.put("user", user);

            // Get user's applications if job seeker
            if (user.getRole() == UserRole.JOB_SEEKER) {
                List<JobApplication> applications = jobApplicationRepository.findByUser(user);
                profile.put("applications", applications);
                profile.put("applicationCount", applications.size());

                // Application success rate for this user
                long successfulApps = applications.stream()
                        .filter(app -> "ACCEPTED".equals(app.getStatus()))
                        .count();
                double userSuccessRate = applications.size() > 0 ? (double) successfulApps / applications.size() * 100
                        : 0;
                profile.put("successRate", Math.round(userSuccessRate * 100.0) / 100.0);
            }

            // Get user's jobs if employer
            if (user.getRole() == UserRole.EMPLOYER) {
                List<JobPost> jobs = jobService.getJobsByEmployerId(user.getId().intValue());
                profile.put("jobs", jobs);
                profile.put("jobCount", jobs.size());

                // Calculate total applications received
                int totalApplicationsReceived = 0;
                for (JobPost job : jobs) {
                    totalApplicationsReceived += applicationService.getJobApplications(job).size();
                }
                profile.put("totalApplicationsReceived", totalApplicationsReceived);
            }

            // Get user's files
            List<FileStorage> files = fileStorageRepository.findByUserIdAndIsActive(userId, true);
            profile.put("files", files);
            profile.put("fileCount", files.size());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Platform Usage Statistics
    @GetMapping("/analytics/platform-usage")
    public ResponseEntity<Map<String, Object>> getPlatformUsageStatistics() {
        try {
            Map<String, Object> usage = new HashMap<>();

            List<User> allUsers = userRepository.findAll();
            List<JobApplication> allApplications = jobApplicationRepository.findAll();
            List<FileStorage> allFiles = fileStorageRepository.findAll();
            List<JobPost> allJobs = jobService.getJobs();

            // User engagement metrics
            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(User::isActive).count();
            double userEngagementRate = totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0;

            // Job market activity
            long totalApplications = allApplications.size();
            double applicationsPerJob = allJobs.size() > 0 ? (double) totalApplications / allJobs.size() : 0;

            // File storage usage
            long totalFiles = allFiles.size();
            long activeFiles = allFiles.stream().filter(FileStorage::isActive).count();

            // Platform efficiency metrics
            Map<String, Object> efficiency = new HashMap<>();
            efficiency.put("userEngagementRate", Math.round(userEngagementRate * 100.0) / 100.0);
            efficiency.put("averageApplicationsPerJob", Math.round(applicationsPerJob * 100.0) / 100.0);
            efficiency.put("fileStorageUtilization",
                    Math.round((double) activeFiles / totalFiles * 100 * 100.0) / 100.0);

            usage.put("totalUsers", totalUsers);
            usage.put("activeUsers", activeUsers);
            usage.put("totalJobs", allJobs.size());
            usage.put("totalApplications", totalApplications);
            usage.put("totalFiles", totalFiles);
            usage.put("activeFiles", activeFiles);
            usage.put("efficiencyMetrics", efficiency);

            return ResponseEntity.ok(usage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}