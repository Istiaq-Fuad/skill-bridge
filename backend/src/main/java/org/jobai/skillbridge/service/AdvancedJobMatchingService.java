package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobRepo;
import org.jobai.skillbridge.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class AdvancedJobMatchingService {

    @Autowired
    private JobRepo jobRepo;

    @Autowired
    private UserRepository userRepository;

    /**
     * Find matching candidates for a job with advanced scoring
     * 
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return
     * @return List of candidates with compatibility scores
     */
    public List<AdvancedCandidateMatch> findMatchingCandidates(Long jobId, int limit) {
        List<AdvancedCandidateMatch> matches = new ArrayList<>();

        try {
            // Get the job
            JobPost job = jobRepo.findById(jobId.intValue()).orElse(null);
            if (job == null) {
                return matches;
            }

            // Get all users and filter by role
            List<User> allUsers = userRepository.findAll();
            List<User> candidates = allUsers.stream()
                    .filter(user -> user.getRole() != null && user.getRole().name().equals("JOB_SEEKER"))
                    .collect(Collectors.toList());

            // Calculate compatibility scores for each candidate
            for (User candidate : candidates) {
                double compatibilityScore = calculateCandidateCompatibility(job, candidate);
                if (compatibilityScore > 0.3) { // Only include candidates with >30% match
                    AdvancedCandidateMatch match = new AdvancedCandidateMatch();
                    match.setCandidate(candidate);
                    match.setJob(job);
                    match.setCompatibilityScore(compatibilityScore);
                    match.setSkillMatchScore(calculateSkillMatch(job, candidate));
                    match.setExperienceMatchScore(calculateExperienceMatch(job, candidate));
                    match.setLocationMatchScore(calculateLocationMatch(job, candidate));
                    matches.add(match);
                }
            }

            // Sort by compatibility score and limit results
            return matches.stream()
                    .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in findMatchingCandidates: " + e.getMessage());
            return matches;
        }
    }

    /**
     * Find matching jobs for a user with advanced scoring
     * 
     * @param userId The user ID
     * @param limit  Maximum number of jobs to return
     * @return List of jobs with compatibility scores
     */
    public List<AdvancedJobMatch> findMatchingJobs(Long userId, int limit) {
        List<AdvancedJobMatch> matches = new ArrayList<>();

        try {
            // Get the user
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return matches;
            }

            // Get all active jobs
            List<JobPost> jobs = jobRepo.findAll();

            // Calculate compatibility scores for each job
            for (JobPost job : jobs) {
                double compatibilityScore = calculateJobCompatibility(user, job);
                if (compatibilityScore > 0.3) { // Only include jobs with >30% match
                    AdvancedJobMatch match = new AdvancedJobMatch();
                    match.setUser(user);
                    match.setJob(job);
                    match.setCompatibilityScore(compatibilityScore);
                    match.setSkillMatchScore(calculateSkillMatch(job, user));
                    match.setExperienceMatchScore(calculateExperienceMatch(job, user));
                    match.setLocationMatchScore(calculateLocationMatch(job, user));
                    matches.add(match);
                }
            }

            // Sort by compatibility score and limit results
            return matches.stream()
                    .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in findMatchingJobs: " + e.getMessage());
            return matches;
        }
    }

    /**
     * Update learning models based on successful placement
     * 
     * @param jobId  Job ID
     * @param userId User ID
     */
    public void updateLearningModels(Long jobId, Long userId) {
        // Implementation for machine learning model updates
        // This would typically involve updating weights and preferences
        // based on successful job placements
        System.out.println("Updating learning models for job " + jobId + " and user " + userId);
    }

    // Private helper methods for scoring
    private double calculateCandidateCompatibility(JobPost job, User candidate) {
        double skillScore = calculateSkillMatch(job, candidate);
        double experienceScore = calculateExperienceMatch(job, candidate);
        double locationScore = calculateLocationMatch(job, candidate);

        // Weighted average
        return (skillScore * 0.5) + (experienceScore * 0.3) + (locationScore * 0.2);
    }

    private double calculateJobCompatibility(User user, JobPost job) {
        return calculateCandidateCompatibility(job, user);
    }

    private double calculateSkillMatch(JobPost job, User candidate) {
        // Simplified skill matching - in a real implementation, this would be more
        // sophisticated
        if (job.getRequirements() == null || candidate.getSkills() == null) {
            return 0.5; // Default score when data is incomplete
        }

        // Basic keyword matching
        String jobRequirements = String.join(" ", job.getRequirements()).toLowerCase();
        long matchingSkills = candidate.getSkills().stream()
                .mapToLong(skill -> jobRequirements.contains(skill.getName().toLowerCase()) ? 1 : 0)
                .sum();

        return Math.min(1.0, (double) matchingSkills / Math.max(1, candidate.getSkills().size()));
    }

    private double calculateExperienceMatch(JobPost job, User candidate) {
        // Simplified experience matching
        if (candidate.getExperiences() == null || candidate.getExperiences().isEmpty()) {
            return 0.3; // Low score for no experience
        }

        // Calculate total years of experience
        int totalExperience = candidate.getExperiences().size() * 2; // Assume 2 years per experience entry

        if (job.getTitle() != null && job.getTitle().toLowerCase().contains("senior")) {
            return totalExperience >= 5 ? 0.9 : 0.4;
        } else if (job.getTitle() != null && job.getTitle().toLowerCase().contains("junior")) {
            return totalExperience <= 3 ? 0.9 : 0.7;
        }

        return 0.7; // Default for mid-level positions
    }

    private double calculateLocationMatch(JobPost job, User candidate) {
        if (job.getLocation() == null || candidate.getCity() == null) {
            return 0.8; // Default score when location data is incomplete
        }

        if (job.getLocation().toLowerCase().contains("remote") ||
                job.getLocation().toLowerCase().contains("anywhere")) {
            return 1.0; // Perfect match for remote work
        }

        String jobLocation = job.getLocation().toLowerCase();
        String candidateLocation = candidate.getCity().toLowerCase();

        return jobLocation.contains(candidateLocation) || candidateLocation.contains(jobLocation) ? 1.0 : 0.3;
    }

    // Result classes
    public static class AdvancedCandidateMatch {
        private User candidate;
        private JobPost job;
        private double compatibilityScore;
        private double skillMatchScore;
        private double experienceMatchScore;
        private double locationMatchScore;

        // Getters and setters
        public User getCandidate() {
            return candidate;
        }

        public void setCandidate(User candidate) {
            this.candidate = candidate;
        }

        public JobPost getJob() {
            return job;
        }

        public void setJob(JobPost job) {
            this.job = job;
        }

        public double getCompatibilityScore() {
            return compatibilityScore;
        }

        public void setCompatibilityScore(double compatibilityScore) {
            this.compatibilityScore = compatibilityScore;
        }

        public double getSkillMatchScore() {
            return skillMatchScore;
        }

        public void setSkillMatchScore(double skillMatchScore) {
            this.skillMatchScore = skillMatchScore;
        }

        public double getExperienceMatchScore() {
            return experienceMatchScore;
        }

        public void setExperienceMatchScore(double experienceMatchScore) {
            this.experienceMatchScore = experienceMatchScore;
        }

        public double getLocationMatchScore() {
            return locationMatchScore;
        }

        public void setLocationMatchScore(double locationMatchScore) {
            this.locationMatchScore = locationMatchScore;
        }
    }

    public static class AdvancedJobMatch {
        private User user;
        private JobPost job;
        private double compatibilityScore;
        private double skillMatchScore;
        private double experienceMatchScore;
        private double locationMatchScore;

        // Getters and setters
        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public JobPost getJob() {
            return job;
        }

        public void setJob(JobPost job) {
            this.job = job;
        }

        public double getCompatibilityScore() {
            return compatibilityScore;
        }

        public void setCompatibilityScore(double compatibilityScore) {
            this.compatibilityScore = compatibilityScore;
        }

        public double getSkillMatchScore() {
            return skillMatchScore;
        }

        public void setSkillMatchScore(double skillMatchScore) {
            this.skillMatchScore = skillMatchScore;
        }

        public double getExperienceMatchScore() {
            return experienceMatchScore;
        }

        public void setExperienceMatchScore(double experienceMatchScore) {
            this.experienceMatchScore = experienceMatchScore;
        }

        public double getLocationMatchScore() {
            return locationMatchScore;
        }

        public void setLocationMatchScore(double locationMatchScore) {
            this.locationMatchScore = locationMatchScore;
        }
    }
}