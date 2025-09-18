package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedEmployerDashboardService {
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private JobRepo jobRepository;
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get comprehensive dashboard statistics for an employer
     * @param employer The employer user
     * @return Enhanced dashboard statistics
     */
    public EnhancedDashboardStats getEnhancedDashboardStats(User employer) {
        // Get employer's jobs
        List<JobPost> employerJobs = jobService.getJobsByEmployer(employer);
        
        // Get all applications for employer's jobs
        List<JobApplication> allApplications = applicationService.getApplicationsForEmployer(employer);
        
        // Calculate enhanced statistics
        EnhancedDashboardStats stats = new EnhancedDashboardStats();
        
        // Basic counts
        stats.setTotalJobs(employerJobs.size());
        stats.setTotalApplications(allApplications.size());
        
        // Applications by status
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("APPLIED", 0);
        statusCounts.put("REVIEWED", 0);
        statusCounts.put("INTERVIEW", 0);
        statusCounts.put("REJECTED", 0);
        statusCounts.put("ACCEPTED", 0);
        
        for (JobApplication application : allApplications) {
            String status = application.getStatus();
            if (status != null) {
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
        }
        stats.setApplicationsByStatus(statusCounts);
        
        // Recent jobs (last 5)
        List<JobPost> recentJobs = employerJobs.stream()
                .sorted((j1, j2) -> j2.getPostedAt().compareTo(j1.getPostedAt()))
                .limit(5)
                .collect(Collectors.toList());
        stats.setRecentJobs(recentJobs);
        
        // Recent applications (last 5)
        List<JobApplication> recentApplications = allApplications.stream()
                .sorted((a1, a2) -> a2.getAppliedAt().compareTo(a1.getAppliedAt()))
                .limit(5)
                .collect(Collectors.toList());
        stats.setRecentApplications(recentApplications);
        
        // Enhanced metrics
        stats.setJobPerformanceMetrics(calculateJobPerformanceMetrics(employerJobs));
        stats.setApplicationTrends(calculateApplicationTrends(allApplications));
        stats.setCandidateDemographics(calculateCandidateDemographics(allApplications));
        stats.setEmployerActivityMetrics(calculateEmployerActivityMetrics(employer, employerJobs));
        
        return stats;
    }
    
    /**
     * Calculate job performance metrics
     * @param jobs Employer's jobs
     * @return Job performance metrics
     */
    private JobPerformanceMetrics calculateJobPerformanceMetrics(List<JobPost> jobs) {
        JobPerformanceMetrics metrics = new JobPerformanceMetrics();
        
        if (jobs.isEmpty()) {
            return metrics;
        }
        
        int totalApplications = 0;
        int totalAccepted = 0;
        long totalDaysPosted = 0;
        int activeJobs = 0;
        
        LocalDateTime now = LocalDateTime.now();
        
        for (JobPost job : jobs) {
            // Get applications for this job
            List<JobApplication> jobApplications = applicationRepository.findByJobPost(job);
            totalApplications += jobApplications.size();
            
            // Count accepted applications
            long acceptedCount = jobApplications.stream()
                    .filter(app -> "ACCEPTED".equals(app.getStatus()))
                    .count();
            totalAccepted += acceptedCount;
            
            // Calculate days posted
            if (job.getPostedAt() != null) {
                long daysPosted = ChronoUnit.DAYS.between(job.getPostedAt(), now);
                totalDaysPosted += daysPosted;
            }
            
            // Count active jobs
            if ("ACTIVE".equals(job.getJobStatus())) {
                activeJobs++;
            }
        }
        
        // Calculate metrics
        metrics.setTotalApplications(totalApplications);
        metrics.setTotalAccepted(totalAccepted);
        metrics.setAverageApplicationsPerJob(jobs.size() > 0 ? (double) totalApplications / jobs.size() : 0);
        metrics.setAcceptanceRate(totalApplications > 0 ? (double) totalAccepted / totalApplications : 0);
        metrics.setAverageDaysToFill(totalAccepted > 0 ? (double) totalDaysPosted / totalAccepted : 0);
        metrics.setActiveJobs(activeJobs);
        metrics.setConversionRate(jobs.size() > 0 ? (double) totalAccepted / jobs.size() : 0);
        
        return metrics;
    }
    
    /**
     * Calculate application trends over time
     * @param applications All applications
     * @return Application trends
     */
    private ApplicationTrends calculateApplicationTrends(List<JobApplication> applications) {
        ApplicationTrends trends = new ApplicationTrends();
        
        if (applications.isEmpty()) {
            return trends;
        }
        
        // Group applications by date (last 30 days)
        Map<LocalDate, Integer> dailyApplications = new HashMap<>();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        
        for (JobApplication application : applications) {
            if (application.getAppliedAt() != null) {
                LocalDate applicationDate = application.getAppliedAt().toLocalDate();
                if (!applicationDate.isBefore(thirtyDaysAgo)) {
                    dailyApplications.put(applicationDate, dailyApplications.getOrDefault(applicationDate, 0) + 1);
                }
            }
        }
        
        trends.setDailyApplications(dailyApplications);
        
        // Calculate weekly trend
        Map<String, Integer> weeklyTrend = new HashMap<>();
        weeklyTrend.put("This Week", 0);
        weeklyTrend.put("Last Week", 0);
        weeklyTrend.put("2 Weeks Ago", 0);
        weeklyTrend.put("3 Weeks Ago", 0);
        
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);
        LocalDate threeWeeksAgo = today.minusDays(21);
        
        for (JobApplication application : applications) {
            if (application.getAppliedAt() != null) {
                LocalDate applicationDate = application.getAppliedAt().toLocalDate();
                if (!applicationDate.isBefore(today) && !applicationDate.isAfter(today)) {
                    weeklyTrend.put("This Week", weeklyTrend.get("This Week") + 1);
                } else if (!applicationDate.isBefore(oneWeekAgo) && !applicationDate.isAfter(today)) {
                    weeklyTrend.put("Last Week", weeklyTrend.get("Last Week") + 1);
                } else if (!applicationDate.isBefore(twoWeeksAgo) && !applicationDate.isAfter(oneWeekAgo)) {
                    weeklyTrend.put("2 Weeks Ago", weeklyTrend.get("2 Weeks Ago") + 1);
                } else if (!applicationDate.isBefore(threeWeeksAgo) && !applicationDate.isAfter(twoWeeksAgo)) {
                    weeklyTrend.put("3 Weeks Ago", weeklyTrend.get("3 Weeks Ago") + 1);
                }
            }
        }
        
        trends.setWeeklyTrend(weeklyTrend);
        
        return trends;
    }
    
    /**
     * Calculate candidate demographics
     * @param applications All applications
     * @return Candidate demographics
     */
    private CandidateDemographics calculateCandidateDemographics(List<JobApplication> applications) {
        CandidateDemographics demographics = new CandidateDemographics();
        
        if (applications.isEmpty()) {
            return demographics;
        }
        
        // Collect candidate users
        Set<User> candidates = new HashSet<>();
        for (JobApplication application : applications) {
            if (application.getUser() != null) {
                candidates.add(application.getUser());
            }
        }
        
        // For now, we'll use placeholder data since we don't have detailed demographic info
        // In a real implementation, this would analyze user profiles for location, experience, etc.
        demographics.setTotalCandidates(candidates.size());
        demographics.setByExperienceLevel(new HashMap<>()); // Placeholder
        demographics.setByLocation(new HashMap<>()); // Placeholder
        demographics.setByEducation(new HashMap<>()); // Placeholder
        
        return demographics;
    }
    
    /**
     * Calculate employer activity metrics
     * @param employer The employer
     * @param jobs Employer's jobs
     * @return Employer activity metrics
     */
    private EmployerActivityMetrics calculateEmployerActivityMetrics(User employer, List<JobPost> jobs) {
        EmployerActivityMetrics metrics = new EmployerActivityMetrics();
        
        // For now, we'll use placeholder data
        // In a real implementation, this would track employer actions like job updates, application reviews, etc.
        metrics.setJobsCreated(jobs.size());
        metrics.setLastActivity(LocalDateTime.now()); // Placeholder
        metrics.setResponseRate(0.85); // Placeholder
        metrics.setAvgResponseTime(24.5); // Placeholder (hours)
        
        return metrics;
    }
    
    /**
     * Get detailed job analytics
     * @param jobId The job ID
     * @param employer The employer
     * @return Job analytics
     */
    public JobAnalytics getJobAnalytics(Long jobId, User employer) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Check if job belongs to employer
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized access to job analytics");
        }
        
        List<JobApplication> applications = applicationRepository.findByJobPost(job);
        
        JobAnalytics analytics = new JobAnalytics();
        analytics.setJob(job);
        analytics.setTotalApplications(applications.size());
        
        // Applications by status
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("APPLIED", 0);
        statusCounts.put("REVIEWED", 0);
        statusCounts.put("INTERVIEW", 0);
        statusCounts.put("REJECTED", 0);
        statusCounts.put("ACCEPTED", 0);
        
        for (JobApplication application : applications) {
            String status = application.getStatus();
            if (status != null) {
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
        }
        analytics.setApplicationsByStatus(statusCounts);
        
        // Application timeline
        Map<LocalDate, Integer> dailyApplications = new HashMap<>();
        for (JobApplication application : applications) {
            if (application.getAppliedAt() != null) {
                LocalDate date = application.getAppliedAt().toLocalDate();
                dailyApplications.put(date, dailyApplications.getOrDefault(date, 0) + 1);
            }
        }
        analytics.setApplicationTimeline(dailyApplications);
        
        // Top candidate sources
        Map<String, Integer> sourceCounts = new HashMap<>();
        for (JobApplication application : applications) {
            String source = application.getSource();
            if (source != null && !source.isEmpty()) {
                sourceCounts.put(source, sourceCounts.getOrDefault(source, 0) + 1);
            } else {
                sourceCounts.put("Unknown", sourceCounts.getOrDefault("Unknown", 0) + 1);
            }
        }
        analytics.setTopSources(sourceCounts);
        
        // Time to hire metrics
        Optional<JobApplication> firstApplication = applications.stream()
                .min(Comparator.comparing(JobApplication::getAppliedAt));
        Optional<JobApplication> acceptedApplication = applications.stream()
                .filter(app -> "ACCEPTED".equals(app.getStatus()))
                .min(Comparator.comparing(JobApplication::getAppliedAt));
        
        if (firstApplication.isPresent() && acceptedApplication.isPresent()) {
            long daysToHire = ChronoUnit.DAYS.between(
                    firstApplication.get().getAppliedAt(), 
                    acceptedApplication.get().getAppliedAt());
            analytics.setDaysToFirstHire((int) daysToHire);
        }
        
        return analytics;
    }
    
    /**
     * Bulk update job statuses
     * @param jobIds List of job IDs
     * @param status New status
     * @param employer The employer
     * @return Number of jobs updated
     */
    public int bulkUpdateJobStatuses(List<Long> jobIds, String status, User employer) {
        int updatedCount = 0;
        
        for (Long jobId : jobIds) {
            JobPost job = jobRepository.findById(jobId).orElse(null);
            if (job != null && job.getEmployer().getId().equals(employer.getId())) {
                job.setJobStatus(status);
                jobRepository.save(job);
                updatedCount++;
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Get candidates for a job with enhanced information
     * @param jobId The job ID
     * @param employer The employer
     * @return List of enhanced candidate information
     */
    public List<EnhancedCandidateInfo> getEnhancedCandidatesForJob(Long jobId, User employer) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Check if job belongs to employer
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized access to job candidates");
        }
        
        List<JobApplication> applications = applicationRepository.findByJobPost(job);
        List<EnhancedCandidateInfo> candidates = new ArrayList<>();
        
        for (JobApplication application : applications) {
            EnhancedCandidateInfo candidateInfo = new EnhancedCandidateInfo();
            candidateInfo.setApplication(application);
            
            User candidate = application.getUser();
            if (candidate != null) {
                candidateInfo.setCandidate(candidate);
                
                // Calculate application age
                if (application.getAppliedAt() != null) {
                    long daysSinceApplication = ChronoUnit.DAYS.between(
                            application.getAppliedAt(), 
                            LocalDateTime.now());
                    candidateInfo.setDaysSinceApplication((int) daysSinceApplication);
                }
                
                // Get candidate skills (placeholder - in real implementation, this would be more detailed)
                candidateInfo.setSkillsCount(candidate.getSkills() != null ? candidate.getSkills().size() : 0);
            }
            
            candidates.add(candidateInfo);
        }
        
        return candidates;
    }
    
    // DTOs for dashboard data
    
    public static class EnhancedDashboardStats {
        private int totalJobs;
        private int totalApplications;
        private Map<String, Integer> applicationsByStatus;
        private List<JobPost> recentJobs;
        private List<JobApplication> recentApplications;
        private JobPerformanceMetrics jobPerformanceMetrics;
        private ApplicationTrends applicationTrends;
        private CandidateDemographics candidateDemographics;
        private EmployerActivityMetrics employerActivityMetrics;
        
        // Getters and setters
        public int getTotalJobs() { return totalJobs; }
        public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }
        
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        
        public Map<String, Integer> getApplicationsByStatus() { return applicationsByStatus; }
        public void setApplicationsByStatus(Map<String, Integer> applicationsByStatus) { this.applicationsByStatus = applicationsByStatus; }
        
        public List<JobPost> getRecentJobs() { return recentJobs; }
        public void setRecentJobs(List<JobPost> recentJobs) { this.recentJobs = recentJobs; }
        
        public List<JobApplication> getRecentApplications() { return recentApplications; }
        public void setRecentApplications(List<JobApplication> recentApplications) { this.recentApplications = recentApplications; }
        
        public JobPerformanceMetrics getJobPerformanceMetrics() { return jobPerformanceMetrics; }
        public void setJobPerformanceMetrics(JobPerformanceMetrics jobPerformanceMetrics) { this.jobPerformanceMetrics = jobPerformanceMetrics; }
        
        public ApplicationTrends getApplicationTrends() { return applicationTrends; }
        public void setApplicationTrends(ApplicationTrends applicationTrends) { this.applicationTrends = applicationTrends; }
        
        public CandidateDemographics getCandidateDemographics() { return candidateDemographics; }
        public void setCandidateDemographics(CandidateDemographics candidateDemographics) { this.candidateDemographics = candidateDemographics; }
        
        public EmployerActivityMetrics getEmployerActivityMetrics() { return employerActivityMetrics; }
        public void setEmployerActivityMetrics(EmployerActivityMetrics employerActivityMetrics) { this.employerActivityMetrics = employerActivityMetrics; }
    }
    
    public static class JobPerformanceMetrics {
        private int totalApplications;
        private int totalAccepted;
        private double averageApplicationsPerJob;
        private double acceptanceRate;
        private double averageDaysToFill;
        private int activeJobs;
        private double conversionRate;
        
        // Getters and setters
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        
        public int getTotalAccepted() { return totalAccepted; }
        public void setTotalAccepted(int totalAccepted) { this.totalAccepted = totalAccepted; }
        
        public double getAverageApplicationsPerJob() { return averageApplicationsPerJob; }
        public void setAverageApplicationsPerJob(double averageApplicationsPerJob) { this.averageApplicationsPerJob = averageApplicationsPerJob; }
        
        public double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public double getAverageDaysToFill() { return averageDaysToFill; }
        public void setAverageDaysToFill(double averageDaysToFill) { this.averageDaysToFill = averageDaysToFill; }
        
        public int getActiveJobs() { return activeJobs; }
        public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
        
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    }
    
    public static class ApplicationTrends {
        private Map<LocalDate, Integer> dailyApplications;
        private Map<String, Integer> weeklyTrend;
        
        // Getters and setters
        public Map<LocalDate, Integer> getDailyApplications() { return dailyApplications; }
        public void setDailyApplications(Map<LocalDate, Integer> dailyApplications) { this.dailyApplications = dailyApplications; }
        
        public Map<String, Integer> getWeeklyTrend() { return weeklyTrend; }
        public void setWeeklyTrend(Map<String, Integer> weeklyTrend) { this.weeklyTrend = weeklyTrend; }
    }
    
    public static class CandidateDemographics {
        private int totalCandidates;
        private Map<String, Integer> byExperienceLevel;
        private Map<String, Integer> byLocation;
        private Map<String, Integer> byEducation;
        
        // Getters and setters
        public int getTotalCandidates() { return totalCandidates; }
        public void setTotalCandidates(int totalCandidates) { this.totalCandidates = totalCandidates; }
        
        public Map<String, Integer> getByExperienceLevel() { return byExperienceLevel; }
        public void setByExperienceLevel(Map<String, Integer> byExperienceLevel) { this.byExperienceLevel = byExperienceLevel; }
        
        public Map<String, Integer> getByLocation() { return byLocation; }
        public void setByLocation(Map<String, Integer> byLocation) { this.byLocation = byLocation; }
        
        public Map<String, Integer> getByEducation() { return byEducation; }
        public void setByEducation(Map<String, Integer> byEducation) { this.byEducation = byEducation; }
    }
    
    public static class EmployerActivityMetrics {
        private int jobsCreated;
        private LocalDateTime lastActivity;
        private double responseRate;
        private double avgResponseTime;
        
        // Getters and setters
        public int getJobsCreated() { return jobsCreated; }
        public void setJobsCreated(int jobsCreated) { this.jobsCreated = jobsCreated; }
        
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
        
        public double getResponseRate() { return responseRate; }
        public void setResponseRate(double responseRate) { this.responseRate = responseRate; }
        
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
    }
    
    public static class JobAnalytics {
        private JobPost job;
        private int totalApplications;
        private Map<String, Integer> applicationsByStatus;
        private Map<LocalDate, Integer> applicationTimeline;
        private Map<String, Integer> topSources;
        private int daysToFirstHire;
        
        // Getters and setters
        public JobPost getJob() { return job; }
        public void setJob(JobPost job) { this.job = job; }
        
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        
        public Map<String, Integer> getApplicationsByStatus() { return applicationsByStatus; }
        public void setApplicationsByStatus(Map<String, Integer> applicationsByStatus) { this.applicationsByStatus = applicationsByStatus; }
        
        public Map<LocalDate, Integer> getApplicationTimeline() { return applicationTimeline; }
        public void setApplicationTimeline(Map<LocalDate, Integer> applicationTimeline) { this.applicationTimeline = applicationTimeline; }
        
        public Map<String, Integer> getTopSources() { return topSources; }
        public void setTopSources(Map<String, Integer> topSources) { this.topSources = topSources; }
        
        public int getDaysToFirstHire() { return daysToFirstHire; }
        public void setDaysToFirstHire(int daysToFirstHire) { this.daysToFirstHire = daysToFirstHire; }
    }
    
    public static class EnhancedCandidateInfo {
        private JobApplication application;
        private User candidate;
        private int daysSinceApplication;
        private int skillsCount;
        
        // Getters and setters
        public JobApplication getApplication() { return application; }
        public void setApplication(JobApplication application) { this.application = application; }
        
        public User getCandidate() { return candidate; }
        public void setCandidate(User candidate) { this.candidate = candidate; }
        
        public int getDaysSinceApplication() { return daysSinceApplication; }
        public void setDaysSinceApplication(int daysSinceApplication) { this.daysSinceApplication = daysSinceApplication; }
        
        public int getSkillsCount() { return skillsCount; }
        public void setSkillsCount(int skillsCount) { this.skillsCount = skillsCount; }
    }
}