package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvancedJobMatchingService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepo jobRepository;
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private ExperienceRepository experienceRepository;
    
    @Autowired
    private EducationRepository educationRepository;
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    @Autowired
    private EmployerProfileRepository employerProfileRepository;
    
    // In-memory storage for learning data (in a real implementation, this would be in a database)
    private Map<String, Double> skillPerformanceWeights = new HashMap<>();
    private Map<String, Double> experienceWeights = new HashMap<>();
    private Map<String, Double> locationWeights = new HashMap<>();
    private Map<String, Double> companyWeights = new HashMap<>();
    
    /**
     * Find candidates that match a job's requirements with advanced scoring and learning
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return
     * @return List of matching candidates with advanced compatibility scores
     */
    public List<AdvancedCandidateMatch> findMatchingCandidates(Long jobId, int limit) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        List<User> allUsers = userRepository.findAll();
        List<AdvancedCandidateMatch> matches = new ArrayList<>();
        
        for (User user : allUsers) {
            // Only consider job seekers
            if (user.getRole() != null && user.getRole().name().equals("JOB_SEEKER")) {
                AdvancedCompatibilityScore score = calculateAdvancedCompatibilityScore(user, job);
                matches.add(new AdvancedCandidateMatch(user, score));
            }
        }
        
        // Sort by compatibility score (highest first) and limit results
        return matches.stream()
                .sorted((m1, m2) -> Double.compare(m2.getScore().getTotalScore(), m1.getScore().getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Find jobs that match a user's profile with advanced scoring and learning
     * @param userId The user ID
     * @param limit Maximum number of jobs to return
     * @return List of matching jobs with advanced compatibility scores
     */
    public List<AdvancedJobMatch> findMatchingJobs(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<JobPost> allJobs = jobRepository.findAll();
        List<AdvancedJobMatch> matches = new ArrayList<>();
        
        for (JobPost job : allJobs) {
            AdvancedCompatibilityScore score = calculateAdvancedCompatibilityScore(user, job);
            matches.add(new AdvancedJobMatch(job, score));
        }
        
        // Sort by compatibility score (highest first) and limit results
        return matches.stream()
                .sorted((m1, m2) -> Double.compare(m2.getScore().getTotalScore(), m1.getScore().getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate advanced compatibility score between user and job with learning
     * @param user The candidate user
     * @param job The job post
     * @return Advanced compatibility score with breakdown
     */
    private AdvancedCompatibilityScore calculateAdvancedCompatibilityScore(User user, JobPost job) {
        // Get user's complete profile data
        List<Skill> userSkills = skillRepository.findByUser(user);
        List<Experience> userExperiences = experienceRepository.findByUser(user);
        List<Education> userEducations = educationRepository.findByUser(user);
        
        // Calculate individual component scores with learning adjustments
        SkillMatchScore skillScore = calculateAdvancedSkillMatchScore(userSkills, job);
        ExperienceMatchScore experienceScore = calculateAdvancedExperienceMatchScore(userExperiences, job);
        EducationMatchScore educationScore = calculateAdvancedEducationMatchScore(userEducations, job);
        CulturalFitScore culturalFitScore = calculateAdvancedCulturalFitScore(user, job);
        MarketDemandScore marketDemandScore = calculateMarketDemandScore(job);
        
        // Calculate weighted total score with learning factors
        double totalScore = calculateAdvancedWeightedTotalScore(
                skillScore, experienceScore, educationScore, culturalFitScore, marketDemandScore);
        
        return new AdvancedCompatibilityScore(
                totalScore,
                skillScore,
                experienceScore,
                educationScore,
                culturalFitScore,
                marketDemandScore
        );
    }
    
    /**
     * Calculate advanced skill match score with learning from past placements
     * @param userSkills User's skills
     * @param job Job post
     * @return Advanced skill match score
     */
    private SkillMatchScore calculateAdvancedSkillMatchScore(List<Skill> userSkills, JobPost job) {
        if (userSkills.isEmpty() || job == null) {
            return new SkillMatchScore(0.0, 0.0, 0.0, 0.0, new ArrayList<>());
        }
        
        List<String> jobSkills = job.getPostTechStack() != null ? job.getPostTechStack() : new ArrayList<>();
        
        if (jobSkills.isEmpty()) {
            // If no specific skills required, return a moderate score
            return new SkillMatchScore(0.5, 0.5, 0.5, 0.5, new ArrayList<>());
        }
        
        List<SkillMatchDetail> skillMatches = new ArrayList<>();
        int exactMatches = 0;
        int partialMatches = 0;
        double totalProficiencyScore = 0.0;
        int matchedSkills = 0;
        
        for (String jobSkill : jobSkills) {
            SkillMatchDetail matchDetail = findBestSkillMatch(jobSkill, userSkills);
            skillMatches.add(matchDetail);
            
            if (matchDetail.getMatchType() == SkillMatchType.EXACT) {
                exactMatches++;
            } else if (matchDetail.getMatchType() == SkillMatchType.PARTIAL) {
                partialMatches++;
            }
            
            if (matchDetail.isMatched()) {
                totalProficiencyScore += matchDetail.getProficiencyScore();
                matchedSkills++;
            }
        }
        
        // Apply learning weights based on past successful placements
        double learningAdjustment = getSkillLearningAdjustment(jobSkills);
        
        // Calculate component scores (0.0 to 1.0 each)
        double skillCoverageScore = (double) (exactMatches + partialMatches) / jobSkills.size();
        double proficiencyScore = matchedSkills > 0 ? totalProficiencyScore / matchedSkills : 0.0;
        double exactMatchRatio = (double) exactMatches / jobSkills.size();
        double weightedScore = (skillCoverageScore * 0.4 + proficiencyScore * 0.4 + exactMatchRatio * 0.2) * learningAdjustment;
        
        return new SkillMatchScore(weightedScore, skillCoverageScore, proficiencyScore, exactMatchRatio, skillMatches);
    }
    
    /**
     * Find the best matching skill for a job requirement
     * @param jobSkill Required skill from job
     * @param userSkills User's skills
     * @return Skill match detail
     */
    private SkillMatchDetail findBestSkillMatch(String jobSkill, List<Skill> userSkills) {
        if (jobSkill == null || userSkills.isEmpty()) {
            return new SkillMatchDetail(jobSkill, null, false, 0.0, SkillMatchType.NONE);
        }
        
        Skill bestMatch = null;
        SkillMatchType bestMatchType = SkillMatchType.NONE;
        double bestScore = 0.0;
        
        String jobSkillLower = jobSkill.toLowerCase().trim();
        
        for (Skill userSkill : userSkills) {
            String userSkillLower = userSkill.getName().toLowerCase().trim();
            
            // Exact match
            if (jobSkillLower.equals(userSkillLower)) {
                return new SkillMatchDetail(
                        jobSkill, 
                        userSkill.getName(), 
                        true, 
                        userSkill.getProficiencyLevel() / 10.0, 
                        SkillMatchType.EXACT
                );
            }
            
            // Partial match (job skill is substring of user skill or vice versa)
            if (jobSkillLower.contains(userSkillLower) || userSkillLower.contains(jobSkillLower)) {
                double score = userSkill.getProficiencyLevel() / 10.0;
                if (score > bestScore) {
                    bestMatch = userSkill;
                    bestScore = score;
                    bestMatchType = SkillMatchType.PARTIAL;
                }
            }
        }
        
        if (bestMatch != null) {
            return new SkillMatchDetail(
                    jobSkill, 
                    bestMatch.getName(), 
                    true, 
                    bestScore, 
                    bestMatchType
            );
        }
        
        return new SkillMatchDetail(jobSkill, null, false, 0.0, SkillMatchType.NONE);
    }
    
    /**
     * Calculate advanced experience match score with learning
     * @param userExperiences User's work experiences
     * @param job Job post
     * @return Advanced experience match score
     */
    private ExperienceMatchScore calculateAdvancedExperienceMatchScore(List<Experience> userExperiences, JobPost job) {
        if (userExperiences.isEmpty() || job == null) {
            return new ExperienceMatchScore(0.0, 0.0, 0.0, 0, 0.0, new ArrayList<>());
        }
        
        int requiredExperience = job.getReqExperience() != null ? job.getReqExperience() : 0;
        double totalExperienceYears = calculateTotalExperienceYears(userExperiences);
        List<RelevantExperience> relevantExperiences = findRelevantExperiences(userExperiences, job);
        
        // Experience duration score (0.0 to 1.0)
        double experienceDurationScore = requiredExperience > 0 ? 
                Math.min(1.0, totalExperienceYears / requiredExperience) : 0.5;
        
        // Relevant experience score
        double relevantExperienceScore = relevantExperiences.size() > 0 ? 
                Math.min(1.0, (double) relevantExperiences.size() / 3.0) : 0.0;
        
        // Recent experience score (experience in last 5 years)
        long recentExperienceCount = relevantExperiences.stream()
                .filter(exp -> exp.getYearsSince() <= 5)
                .count();
        double recentExperienceScore = relevantExperiences.size() > 0 ? 
                (double) recentExperienceCount / relevantExperiences.size() : 0.0;
        
        // Apply learning weights based on past successful placements
        double learningAdjustment = getExperienceLearningAdjustment(requiredExperience);
        
        // Weighted score
        double weightedScore = (experienceDurationScore * 0.4 + relevantExperienceScore * 0.4 + recentExperienceScore * 0.2) * learningAdjustment;
        
        return new ExperienceMatchScore(
                weightedScore,
                experienceDurationScore,
                relevantExperienceScore,
                (int) totalExperienceYears,
                recentExperienceScore,
                relevantExperiences
        );
    }
    
    /**
     * Calculate total experience years from user's work history
     * @param experiences User's experiences
     * @return Total experience in years
     */
    private double calculateTotalExperienceYears(List<Experience> experiences) {
        double totalYears = 0.0;
        
        for (Experience exp : experiences) {
            if (exp.getStartDate() != null) {
                LocalDate endDate = exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now();
                long months = java.time.temporal.ChronoUnit.MONTHS.between(exp.getStartDate(), endDate);
                totalYears += months / 12.0;
            }
        }
        
        return totalYears;
    }
    
    /**
     * Find experiences relevant to the job
     * @param experiences User's experiences
     * @param job Job post
     * @return List of relevant experiences
     */
    private List<RelevantExperience> findRelevantExperiences(List<Experience> experiences, JobPost job) {
        List<RelevantExperience> relevant = new ArrayList<>();
        List<String> jobSkills = job.getPostTechStack() != null ? job.getPostTechStack() : new ArrayList<>();
        String jobTitle = job.getPostProfile() != null ? job.getPostProfile().toLowerCase() : "";
        
        for (Experience exp : experiences) {
            // Check if experience is relevant based on job title keywords or skills
            boolean isRelevant = false;
            String relevanceReason = "";
            
            if (exp.getPosition() != null && exp.getPosition().toLowerCase().contains(jobTitle)) {
                isRelevant = true;
                relevanceReason = "Position matches job title";
            } else if (exp.getDescription() != null) {
                String descLower = exp.getDescription().toLowerCase();
                for (String skill : jobSkills) {
                    if (descLower.contains(skill.toLowerCase())) {
                        isRelevant = true;
                        relevanceReason = "Description mentions required skill: " + skill;
                        break;
                    }
                }
            }
            
            if (isRelevant) {
                LocalDate endDate = exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now();
                long yearsSince = java.time.temporal.ChronoUnit.YEARS.between(endDate, LocalDate.now());
                
                relevant.add(new RelevantExperience(
                        exp.getPosition(),
                        exp.getCompany(),
                        exp.getStartDate(),
                        exp.getEndDate(),
                        yearsSince,
                        relevanceReason
                ));
            }
        }
        
        return relevant;
    }
    
    /**
     * Calculate advanced education match score with learning
     * @param userEducations User's educations
     * @param job Job post
     * @return Advanced education match score
     */
    private EducationMatchScore calculateAdvancedEducationMatchScore(List<Education> userEducations, JobPost job) {
        if (userEducations.isEmpty() || job == null) {
            return new EducationMatchScore(0.0, 0.0, 0.0, null);
        }
        
        // For now, we'll use a simple approach - in a real implementation, 
        // this would check field of study, degree level, institution prestige, etc.
        Education highestEducation = findHighestEducation(userEducations);
        
        // Education presence score (0.0 to 1.0)
        double educationPresenceScore = 0.7; // Default score
        
        // Degree level score (0.0 to 1.0)
        double degreeLevelScore = 0.5; // Default moderate score
        
        if (highestEducation != null) {
            String degree = highestEducation.getDegree() != null ? highestEducation.getDegree().toLowerCase() : "";
            if (degree.contains("phd") || degree.contains("doctor")) {
                degreeLevelScore = 1.0;
            } else if (degree.contains("master") || degree.contains("mba")) {
                degreeLevelScore = 0.8;
            } else if (degree.contains("bachelor") || degree.contains("bs") || degree.contains("ba")) {
                degreeLevelScore = 0.6;
            }
        }
        
        // Apply learning weights based on past successful placements
        double learningAdjustment = getEducationLearningAdjustment();
        
        // Weighted score
        double weightedScore = (educationPresenceScore * 0.3 + degreeLevelScore * 0.4 + 0.3) * learningAdjustment;
        
        return new EducationMatchScore(
                weightedScore,
                educationPresenceScore,
                degreeLevelScore,
                highestEducation
        );
    }
    
    /**
     * Find the highest level of education
     * @param educations User's educations
     * @return Highest education
     */
    private Education findHighestEducation(List<Education> educations) {
        if (educations.isEmpty()) {
            return null;
        }
        
        // Simple approach - return the first one
        // In a real implementation, this would rank by degree level
        return educations.get(0);
    }
    
    /**
     * Calculate advanced cultural fit score with learning
     * @param user Candidate user
     * @param job Job post
     * @return Advanced cultural fit score
     */
    private CulturalFitScore calculateAdvancedCulturalFitScore(User user, JobPost job) {
        // For now, we'll use a simple approach based on location and bio keywords
        // In a real implementation, this would use more sophisticated NLP and behavioral analysis
        
        double locationScore = 1.0; // Default score
        if (user.getCity() != null && job.getLocation() != null) {
            // Simple location matching - in real implementation, this would consider distance, remote work, etc.
            locationScore = user.getCity().equalsIgnoreCase(job.getLocation()) ? 1.0 : 0.7;
        }
        
        double bioScore = 0.5; // Default moderate score
        if (user.getBio() != null) {
            // Simple keyword matching - in real implementation, this would use NLP sentiment analysis
            String bioLower = user.getBio().toLowerCase();
            String[] positiveKeywords = {"team", "collaborative", "innovative", "passionate", "dedicated"};
            int matchingKeywords = 0;
            
            for (String keyword : positiveKeywords) {
                if (bioLower.contains(keyword)) {
                    matchingKeywords++;
                }
            }
            
            bioScore = (double) matchingKeywords / positiveKeywords.length;
        }
        
        // Apply learning weights based on past successful placements
        double learningAdjustment = getCulturalFitLearningAdjustment();
        
        // Weighted score
        double weightedScore = (locationScore * 0.6 + bioScore * 0.4) * learningAdjustment;
        
        return new CulturalFitScore(weightedScore, locationScore, bioScore);
    }
    
    /**
     * Calculate market demand score based on job popularity and competition
     * @param job Job post
     * @return Market demand score
     */
    private MarketDemandScore calculateMarketDemandScore(JobPost job) {
        // Get number of applications for this job
        List<JobApplication> applications = applicationRepository.findByJobPost(job);
        int applicationCount = applications.size();
        
        // Get number of similar jobs
        List<JobPost> similarJobs = jobRepository.findByPostProfileContainingIgnoreCaseOrPostDescContainingIgnoreCase(
                job.getPostProfile() != null ? job.getPostProfile() : "",
                job.getPostDesc() != null ? job.getPostDesc() : "");
        int similarJobCount = similarJobs.size();
        
        // Market demand score (0.0 to 1.0)
        // Higher score for jobs with fewer applications relative to similar jobs
        double marketDemandScore = similarJobCount > 0 ? 
                Math.min(1.0, (double) similarJobCount / (applicationCount + 1)) : 0.5;
        
        // Competition score (0.0 to 1.0)
        // Lower score for jobs with many applications
        double competitionScore = applicationCount > 0 ? 
                Math.min(1.0, 1.0 / (applicationCount / 10.0 + 1)) : 1.0;
        
        // Trending score based on recent applications
        long recentApplications = applications.stream()
                .filter(app -> app.getAppliedAt() != null && 
                        app.getAppliedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .count();
        double trendingScore = Math.min(1.0, (double) recentApplications / 10.0);
        
        // Weighted score
        double weightedScore = marketDemandScore * 0.4 + competitionScore * 0.4 + trendingScore * 0.2;
        
        return new MarketDemandScore(weightedScore, marketDemandScore, competitionScore, trendingScore);
    }
    
    /**
     * Calculate advanced weighted total score with learning factors
     * @param skillScore Skill match score
     * @param experienceScore Experience match score
     * @param educationScore Education match score
     * @param culturalFitScore Cultural fit score
     * @param marketDemandScore Market demand score
     * @return Weighted total score
     */
    private double calculateAdvancedWeightedTotalScore(
            SkillMatchScore skillScore,
            ExperienceMatchScore experienceScore,
            EducationMatchScore educationScore,
            CulturalFitScore culturalFitScore,
            MarketDemandScore marketDemandScore) {
        
        // Base weighted score
        double weightedScore = (skillScore.getWeightedScore() * 0.35 +
                               experienceScore.getWeightedScore() * 0.25 +
                               educationScore.getWeightedScore() * 0.15 +
                               culturalFitScore.getWeightedScore() * 0.15 +
                               marketDemandScore.getWeightedScore() * 0.10);
        
        return Math.max(0.0, weightedScore); // Ensure non-negative score
    }
    
    /**
     * Update learning models based on successful placements
     * @param jobId Job ID of successful placement
     * @param userId User ID of successful candidate
     */
    public void updateLearningModels(Long jobId, Long userId) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's skills
        List<Skill> userSkills = skillRepository.findByUser(user);
        
        // Update skill performance weights
        List<String> jobSkills = job.getPostTechStack() != null ? job.getPostTechStack() : new ArrayList<>();
        for (String skill : jobSkills) {
            skillPerformanceWeights.put(skill, 
                    skillPerformanceWeights.getOrDefault(skill, 0.5) + 0.1);
        }
        
        // Update experience weights
        int requiredExperience = job.getReqExperience() != null ? job.getReqExperience() : 0;
        experienceWeights.put("exp_" + requiredExperience, 
                experienceWeights.getOrDefault("exp_" + requiredExperience, 0.5) + 0.1);
        
        // Update location weights
        if (job.getLocation() != null) {
            locationWeights.put(job.getLocation(), 
                    locationWeights.getOrDefault(job.getLocation(), 0.5) + 0.1);
        }
        
        // Update company weights
        if (job.getEmployer() != null) {
            // Get employer profile to get company name
            Optional<EmployerProfile> employerProfile = employerProfileRepository.findByUser(job.getEmployer());
            if (employerProfile.isPresent() && employerProfile.get().getCompanyName() != null) {
                companyWeights.put(employerProfile.get().getCompanyName(), 
                        companyWeights.getOrDefault(employerProfile.get().getCompanyName(), 0.5) + 0.1);
            }
        }
    }
    
    /**
     * Get skill learning adjustment factor
     * @param jobSkills Required skills for the job
     * @return Adjustment factor
     */
    private double getSkillLearningAdjustment(List<String> jobSkills) {
        double totalWeight = 0.0;
        int count = 0;
        
        for (String skill : jobSkills) {
            Double weight = skillPerformanceWeights.get(skill);
            if (weight != null) {
                totalWeight += weight;
                count++;
            }
        }
        
        // Return average weight or default 1.0 if no data
        return count > 0 ? totalWeight / count : 1.0;
    }
    
    /**
     * Get experience learning adjustment factor
     * @param requiredExperience Required experience years
     * @return Adjustment factor
     */
    private double getExperienceLearningAdjustment(int requiredExperience) {
        Double weight = experienceWeights.get("exp_" + requiredExperience);
        return weight != null ? weight : 1.0;
    }
    
    /**
     * Get education learning adjustment factor
     * @return Adjustment factor
     */
    private double getEducationLearningAdjustment() {
        // In a real implementation, this would be more sophisticated
        return 1.0;
    }
    
    /**
     * Get cultural fit learning adjustment factor
     * @return Adjustment factor
     */
    private double getCulturalFitLearningAdjustment() {
        // In a real implementation, this would be more sophisticated
        return 1.0;
    }
    
    // DTOs for advanced scoring
    
    public static class AdvancedCandidateMatch {
        private User candidate;
        private AdvancedCompatibilityScore score;
        
        public AdvancedCandidateMatch(User candidate, AdvancedCompatibilityScore score) {
            this.candidate = candidate;
            this.score = score;
        }
        
        public User getCandidate() { return candidate; }
        public void setCandidate(User candidate) { this.candidate = candidate; }
        
        public AdvancedCompatibilityScore getScore() { return score; }
        public void setScore(AdvancedCompatibilityScore score) { this.score = score; }
    }
    
    public static class AdvancedJobMatch {
        private JobPost job;
        private AdvancedCompatibilityScore score;
        
        public AdvancedJobMatch(JobPost job, AdvancedCompatibilityScore score) {
            this.job = job;
            this.score = score;
        }
        
        public JobPost getJob() { return job; }
        public void setJob(JobPost job) { this.job = job; }
        
        public AdvancedCompatibilityScore getScore() { return score; }
        public void setScore(AdvancedCompatibilityScore score) { this.score = score; }
    }
    
    public static class AdvancedCompatibilityScore {
        private double totalScore;
        private SkillMatchScore skillScore;
        private ExperienceMatchScore experienceScore;
        private EducationMatchScore educationScore;
        private CulturalFitScore culturalFitScore;
        private MarketDemandScore marketDemandScore;
        
        public AdvancedCompatibilityScore(
                double totalScore,
                SkillMatchScore skillScore,
                ExperienceMatchScore experienceScore,
                EducationMatchScore educationScore,
                CulturalFitScore culturalFitScore,
                MarketDemandScore marketDemandScore) {
            this.totalScore = totalScore;
            this.skillScore = skillScore;
            this.experienceScore = experienceScore;
            this.educationScore = educationScore;
            this.culturalFitScore = culturalFitScore;
            this.marketDemandScore = marketDemandScore;
        }
        
        public double getTotalScore() { return totalScore; }
        public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
        
        public SkillMatchScore getSkillScore() { return skillScore; }
        public void setSkillScore(SkillMatchScore skillScore) { this.skillScore = skillScore; }
        
        public ExperienceMatchScore getExperienceScore() { return experienceScore; }
        public void setExperienceScore(ExperienceMatchScore experienceScore) { this.experienceScore = experienceScore; }
        
        public EducationMatchScore getEducationScore() { return educationScore; }
        public void setEducationScore(EducationMatchScore educationScore) { this.educationScore = educationScore; }
        
        public CulturalFitScore getCulturalFitScore() { return culturalFitScore; }
        public void setCulturalFitScore(CulturalFitScore culturalFitScore) { this.culturalFitScore = culturalFitScore; }
        
        public MarketDemandScore getMarketDemandScore() { return marketDemandScore; }
        public void setMarketDemandScore(MarketDemandScore marketDemandScore) { this.marketDemandScore = marketDemandScore; }
    }
    
    public static class SkillMatchScore {
        private double weightedScore;
        private double skillCoverageScore;
        private double proficiencyScore;
        private double exactMatchRatio;
        private List<SkillMatchDetail> skillMatches;
        
        public SkillMatchScore(
                double weightedScore,
                double skillCoverageScore,
                double proficiencyScore,
                double exactMatchRatio,
                List<SkillMatchDetail> skillMatches) {
            this.weightedScore = weightedScore;
            this.skillCoverageScore = skillCoverageScore;
            this.proficiencyScore = proficiencyScore;
            this.exactMatchRatio = exactMatchRatio;
            this.skillMatches = skillMatches;
        }
        
        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
        
        public double getSkillCoverageScore() { return skillCoverageScore; }
        public void setSkillCoverageScore(double skillCoverageScore) { this.skillCoverageScore = skillCoverageScore; }
        
        public double getProficiencyScore() { return proficiencyScore; }
        public void setProficiencyScore(double proficiencyScore) { this.proficiencyScore = proficiencyScore; }
        
        public double getExactMatchRatio() { return exactMatchRatio; }
        public void setExactMatchRatio(double exactMatchRatio) { this.exactMatchRatio = exactMatchRatio; }
        
        public List<SkillMatchDetail> getSkillMatches() { return skillMatches; }
        public void setSkillMatches(List<SkillMatchDetail> skillMatches) { this.skillMatches = skillMatches; }
    }
    
    public enum SkillMatchType {
        EXACT, PARTIAL, NONE
    }
    
    public static class SkillMatchDetail {
        private String requiredSkill;
        private String matchedSkill;
        private boolean matched;
        private double proficiencyScore;
        private SkillMatchType matchType;
        
        public SkillMatchDetail(
                String requiredSkill,
                String matchedSkill,
                boolean matched,
                double proficiencyScore,
                SkillMatchType matchType) {
            this.requiredSkill = requiredSkill;
            this.matchedSkill = matchedSkill;
            this.matched = matched;
            this.proficiencyScore = proficiencyScore;
            this.matchType = matchType;
        }
        
        public String getRequiredSkill() { return requiredSkill; }
        public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }
        
        public String getMatchedSkill() { return matchedSkill; }
        public void setMatchedSkill(String matchedSkill) { this.matchedSkill = matchedSkill; }
        
        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
        
        public double getProficiencyScore() { return proficiencyScore; }
        public void setProficiencyScore(double proficiencyScore) { this.proficiencyScore = proficiencyScore; }
        
        public SkillMatchType getMatchType() { return matchType; }
        public void setMatchType(SkillMatchType matchType) { this.matchType = matchType; }
    }
    
    public static class ExperienceMatchScore {
        private double weightedScore;
        private double experienceDurationScore;
        private double relevantExperienceScore;
        private int totalExperienceYears;
        private double recentExperienceScore;
        private List<RelevantExperience> relevantExperiences;
        
        public ExperienceMatchScore(
                double weightedScore,
                double experienceDurationScore,
                double relevantExperienceScore,
                int totalExperienceYears,
                double recentExperienceScore,
                List<RelevantExperience> relevantExperiences) {
            this.weightedScore = weightedScore;
            this.experienceDurationScore = experienceDurationScore;
            this.relevantExperienceScore = relevantExperienceScore;
            this.totalExperienceYears = totalExperienceYears;
            this.recentExperienceScore = recentExperienceScore;
            this.relevantExperiences = relevantExperiences;
        }
        
        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
        
        public double getExperienceDurationScore() { return experienceDurationScore; }
        public void setExperienceDurationScore(double experienceDurationScore) { this.experienceDurationScore = experienceDurationScore; }
        
        public double getRelevantExperienceScore() { return relevantExperienceScore; }
        public void setRelevantExperienceScore(double relevantExperienceScore) { this.relevantExperienceScore = relevantExperienceScore; }
        
        public int getTotalExperienceYears() { return totalExperienceYears; }
        public void setTotalExperienceYears(int totalExperienceYears) { this.totalExperienceYears = totalExperienceYears; }
        
        public double getRecentExperienceScore() { return recentExperienceScore; }
        public void setRecentExperienceScore(double recentExperienceScore) { this.recentExperienceScore = recentExperienceScore; }
        
        public List<RelevantExperience> getRelevantExperiences() { return relevantExperiences; }
        public void setRelevantExperiences(List<RelevantExperience> relevantExperiences) { this.relevantExperiences = relevantExperiences; }
    }
    
    public static class RelevantExperience {
        private String position;
        private String company;
        private LocalDate startDate;
        private LocalDate endDate;
        private long yearsSince;
        private String relevanceReason;
        
        public RelevantExperience(
                String position,
                String company,
                LocalDate startDate,
                LocalDate endDate,
                long yearsSince,
                String relevanceReason) {
            this.position = position;
            this.company = company;
            this.startDate = startDate;
            this.endDate = endDate;
            this.yearsSince = yearsSince;
            this.relevanceReason = relevanceReason;
        }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public long getYearsSince() { return yearsSince; }
        public void setYearsSince(long yearsSince) { this.yearsSince = yearsSince; }
        
        public String getRelevanceReason() { return relevanceReason; }
        public void setRelevanceReason(String relevanceReason) { this.relevanceReason = relevanceReason; }
    }
    
    public static class EducationMatchScore {
        private double weightedScore;
        private double educationPresenceScore;
        private double degreeLevelScore;
        private Education highestEducation;
        
        public EducationMatchScore(
                double weightedScore,
                double educationPresenceScore,
                double degreeLevelScore,
                Education highestEducation) {
            this.weightedScore = weightedScore;
            this.educationPresenceScore = educationPresenceScore;
            this.degreeLevelScore = degreeLevelScore;
            this.highestEducation = highestEducation;
        }
        
        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
        
        public double getEducationPresenceScore() { return educationPresenceScore; }
        public void setEducationPresenceScore(double educationPresenceScore) { this.educationPresenceScore = educationPresenceScore; }
        
        public double getDegreeLevelScore() { return degreeLevelScore; }
        public void setDegreeLevelScore(double degreeLevelScore) { this.degreeLevelScore = degreeLevelScore; }
        
        public Education getHighestEducation() { return highestEducation; }
        public void setHighestEducation(Education highestEducation) { this.highestEducation = highestEducation; }
    }
    
    public static class CulturalFitScore {
        private double weightedScore;
        private double locationScore;
        private double bioScore;
        
        public CulturalFitScore(double weightedScore, double locationScore, double bioScore) {
            this.weightedScore = weightedScore;
            this.locationScore = locationScore;
            this.bioScore = bioScore;
        }
        
        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
        
        public double getLocationScore() { return locationScore; }
        public void setLocationScore(double locationScore) { this.locationScore = locationScore; }
        
        public double getBioScore() { return bioScore; }
        public void setBioScore(double bioScore) { this.bioScore = bioScore; }
    }
    
    public static class MarketDemandScore {
        private double weightedScore;
        private double marketDemandScore;
        private double competitionScore;
        private double trendingScore;
        
        public MarketDemandScore(double weightedScore, double marketDemandScore, double competitionScore, double trendingScore) {
            this.weightedScore = weightedScore;
            this.marketDemandScore = marketDemandScore;
            this.competitionScore = competitionScore;
            this.trendingScore = trendingScore;
        }
        
        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
        
        public double getMarketDemandScore() { return marketDemandScore; }
        public void setMarketDemandScore(double marketDemandScore) { this.marketDemandScore = marketDemandScore; }
        
        public double getCompetitionScore() { return competitionScore; }
        public void setCompetitionScore(double competitionScore) { this.competitionScore = competitionScore; }
        
        public double getTrendingScore() { return trendingScore; }
        public void setTrendingScore(double trendingScore) { this.trendingScore = trendingScore; }
    }
}