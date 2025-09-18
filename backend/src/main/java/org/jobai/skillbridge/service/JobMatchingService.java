package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.repo.JobRepo;
import org.jobai.skillbridge.repo.SkillRepository;
import org.jobai.skillbridge.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepo jobRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    /**
     * Find jobs that match a user's skills and experience
     * @param userId The user ID
     * @param limit Maximum number of jobs to return
     * @return List of matching jobs with compatibility scores
     */
    public List<JobMatch> findMatchingJobs(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Skill> userSkills = skillRepository.findByUser(user);
        List<JobPost> allJobs = jobRepository.findAll();
        
        List<JobMatch> matches = new ArrayList<>();
        
        for (JobPost job : allJobs) {
            double compatibilityScore = calculateCompatibilityScore(userSkills, job);
            matches.add(new JobMatch(job, compatibilityScore));
        }
        
        // Sort by compatibility score (highest first) and limit results
        return matches.stream()
                .sorted((m1, m2) -> Double.compare(m2.getCompatibilityScore(), m1.getCompatibilityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Find candidates that match a job's requirements
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return
     * @return List of matching candidates with compatibility scores
     */
    public List<CandidateMatch> findMatchingCandidates(Long jobId, int limit) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        List<User> allUsers = userRepository.findAll();
        List<CandidateMatch> matches = new ArrayList<>();
        
        for (User user : allUsers) {
            // Only consider job seekers
            if (user.getRole() != null && user.getRole().name().equals("JOB_SEEKER")) {
                List<Skill> userSkills = skillRepository.findByUser(user);
                double compatibilityScore = calculateCompatibilityScore(userSkills, job);
                matches.add(new CandidateMatch(user, compatibilityScore));
            }
        }
        
        // Sort by compatibility score (highest first) and limit results
        return matches.stream()
                .sorted((m1, m2) -> Double.compare(m2.getCompatibilityScore(), m1.getCompatibilityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate compatibility score between user skills and job requirements
     * @param userSkills User's skills
     * @param job Job post
     * @return Compatibility score (0.0 to 1.0)
     */
    private double calculateCompatibilityScore(List<Skill> userSkills, JobPost job) {
        if (userSkills.isEmpty() || job == null) {
            return 0.0;
        }
        
        // Get job's required skills from tech stack
        List<String> jobSkills = job.getPostTechStack() != null ? job.getPostTechStack() : new ArrayList<>();
        
        if (jobSkills.isEmpty()) {
            // If no specific skills required, return a moderate score
            return 0.5;
        }
        
        // Calculate skill match score
        int matchingSkills = 0;
        int totalJobSkills = jobSkills.size();
        
        for (String jobSkill : jobSkills) {
            for (Skill userSkill : userSkills) {
                if (isSkillMatch(jobSkill, userSkill.getName())) {
                    matchingSkills++;
                    break;
                }
            }
        }
        
        // Skill match score (0.0 to 0.7)
        double skillMatchScore = totalJobSkills > 0 ? (double) matchingSkills / totalJobSkills * 0.7 : 0;
        
        // Experience match score (0.0 to 0.3)
        double experienceMatchScore = calculateExperienceMatch(userSkills, job);
        
        return Math.min(1.0, skillMatchScore + experienceMatchScore);
    }
    
    /**
     * Check if two skills match (case insensitive, partial matching)
     * @param jobSkill Job required skill
     * @param userSkill User's skill
     * @return True if skills match
     */
    private boolean isSkillMatch(String jobSkill, String userSkill) {
        if (jobSkill == null || userSkill == null) {
            return false;
        }
        
        String jobSkillLower = jobSkill.toLowerCase().trim();
        String userSkillLower = userSkill.toLowerCase().trim();
        
        // Exact match
        if (jobSkillLower.equals(userSkillLower)) {
            return true;
        }
        
        // Partial match (job skill is substring of user skill or vice versa)
        return jobSkillLower.contains(userSkillLower) || userSkillLower.contains(jobSkillLower);
    }
    
    /**
     * Calculate experience match score
     * @param userSkills User's skills
     * @param job Job post
     * @return Experience match score (0.0 to 0.3)
     */
    private double calculateExperienceMatch(List<Skill> userSkills, JobPost job) {
        if (job.getReqExperience() == null || job.getReqExperience() <= 0) {
            return 0.15; // Default score if no experience requirement
        }
        
        // Find the highest proficiency level among matching skills
        int maxProficiency = 0;
        int matchingSkills = 0;
        
        List<String> jobSkills = job.getPostTechStack() != null ? job.getPostTechStack() : new ArrayList<>();
        
        for (String jobSkill : jobSkills) {
            for (Skill userSkill : userSkills) {
                if (isSkillMatch(jobSkill, userSkill.getName())) {
                    matchingSkills++;
                    maxProficiency = Math.max(maxProficiency, userSkill.getProficiencyLevel());
                    break;
                }
            }
        }
        
        if (matchingSkills == 0) {
            return 0.0;
        }
        
        // Calculate experience score based on proficiency and required experience
        double proficiencyScore = (double) maxProficiency / 10.0; // Normalize to 0-1
        double experienceRatio = (double) job.getReqExperience() / 5.0; // Assume 5 years as mid-point
        double experienceScore = Math.min(1.0, proficiencyScore / experienceRatio);
        
        return experienceScore * 0.3; // Scale to 0-0.3
    }
    
    /**
     * DTO for job matching results
     */
    public static class JobMatch {
        private JobPost job;
        private double compatibilityScore;
        
        public JobMatch(JobPost job, double compatibilityScore) {
            this.job = job;
            this.compatibilityScore = compatibilityScore;
        }
        
        public JobPost getJob() {
            return job;
        }
        
        public double getCompatibilityScore() {
            return compatibilityScore;
        }
    }
    
    /**
     * DTO for candidate matching results
     */
    public static class CandidateMatch {
        private User candidate;
        private double compatibilityScore;
        
        public CandidateMatch(User candidate, double compatibilityScore) {
            this.candidate = candidate;
            this.compatibilityScore = compatibilityScore;
        }
        
        public User getCandidate() {
            return candidate;
        }
        
        public double getCompatibilityScore() {
            return compatibilityScore;
        }
    }
}