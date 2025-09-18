package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.repo.JobRepo;
import org.jobai.skillbridge.repo.SkillRepository;
import org.jobai.skillbridge.repo.UserRepository;
import org.jobai.skillbridge.repo.ExperienceRepository;
import org.jobai.skillbridge.repo.EducationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedJobMatchingService {
    
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
    
    /**
     * Find candidates that match a job's requirements with enhanced scoring
     * @param jobId The job ID
     * @param limit Maximum number of candidates to return
     * @return List of matching candidates with detailed compatibility scores
     */
    public List<DetailedCandidateMatch> findMatchingCandidates(Long jobId, int limit) {
        JobPost job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        List<User> allUsers = userRepository.findAll();
        List<DetailedCandidateMatch> matches = new ArrayList<>();
        
        for (User user : allUsers) {
            // Only consider job seekers
            if (user.getRole() != null && user.getRole().name().equals("JOB_SEEKER")) {
                DetailedCompatibilityScore score = calculateDetailedCompatibilityScore(user, job);
                matches.add(new DetailedCandidateMatch(user, score));
            }
        }
        
        // Sort by total compatibility score (highest first) and limit results
        return matches.stream()
                .sorted((m1, m2) -> Double.compare(m2.getScore().getTotalScore(), m1.getScore().getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate detailed compatibility score between user and job requirements
     * @param user The candidate user
     * @param job The job post
     * @return Detailed compatibility score with breakdown
     */
    private DetailedCompatibilityScore calculateDetailedCompatibilityScore(User user, JobPost job) {
        // Get user's complete profile data
        List<Skill> userSkills = skillRepository.findByUser(user);
        List<Experience> userExperiences = experienceRepository.findByUser(user);
        List<Education> userEducations = educationRepository.findByUser(user);
        
        // Calculate individual component scores
        SkillMatchScore skillScore = calculateSkillMatchScore(userSkills, job);
        ExperienceMatchScore experienceScore = calculateExperienceMatchScore(userExperiences, job);
        EducationMatchScore educationScore = calculateEducationMatchScore(userEducations, job);
        CulturalFitScore culturalFitScore = calculateCulturalFitScore(user, job);
        RedFlagDetection redFlags = detectRedFlags(userExperiences, userSkills);
        
        // Calculate weighted total score
        double totalScore = calculateWeightedTotalScore(skillScore, experienceScore, educationScore, culturalFitScore, redFlags);
        
        return new DetailedCompatibilityScore(
                totalScore,
                skillScore,
                experienceScore,
                educationScore,
                culturalFitScore,
                redFlags
        );
    }
    
    /**
     * Calculate skill match score with detailed breakdown
     * @param userSkills User's skills
     * @param job Job post
     * @return Skill match score with details
     */
    public SkillMatchScore calculateSkillMatchScore(List<Skill> userSkills, JobPost job) {
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
        
        // Calculate component scores (0.0 to 1.0 each)
        double skillCoverageScore = (double) (exactMatches + partialMatches) / jobSkills.size();
        double proficiencyScore = matchedSkills > 0 ? totalProficiencyScore / matchedSkills : 0.0;
        double exactMatchRatio = (double) exactMatches / jobSkills.size();
        double weightedScore = (skillCoverageScore * 0.4 + proficiencyScore * 0.4 + exactMatchRatio * 0.2);
        
        return new SkillMatchScore(weightedScore, skillCoverageScore, proficiencyScore, exactMatchRatio, skillMatches);
    }
    
    /**
     * Find the best matching skill for a job requirement
     * @param jobSkill Required skill from job
     * @param userSkills User's skills
     * @return Skill match detail
     */
    public SkillMatchDetail findBestSkillMatch(String jobSkill, List<Skill> userSkills) {
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
     * Calculate experience match score
     * @param userExperiences User's work experiences
     * @param job Job post
     * @return Experience match score with details
     */
    public ExperienceMatchScore calculateExperienceMatchScore(List<Experience> userExperiences, JobPost job) {
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
        
        // Weighted score
        double weightedScore = (experienceDurationScore * 0.4 + relevantExperienceScore * 0.4 + recentExperienceScore * 0.2);
        
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
    public double calculateTotalExperienceYears(List<Experience> experiences) {
        double totalYears = 0.0;
        
        for (Experience exp : experiences) {
            if (exp.getStartDate() != null) {
                LocalDate endDate = exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now();
                long months = ChronoUnit.MONTHS.between(exp.getStartDate(), endDate);
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
    public List<RelevantExperience> findRelevantExperiences(List<Experience> experiences, JobPost job) {
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
                long yearsSince = ChronoUnit.YEARS.between(endDate, LocalDate.now());
                
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
     * Calculate education match score
     * @param userEducations User's educations
     * @param job Job post
     * @return Education match score with details
     */
    private EducationMatchScore calculateEducationMatchScore(List<Education> userEducations, JobPost job) {
        if (userEducations.isEmpty() || job == null) {
            return new EducationMatchScore(0.0, 0.0, 0.0, null);
        }
        
        // For now, we'll use a simple approach - in a real implementation, 
        // this would check field of study, degree level, institution prestige, etc.
        Education highestEducation = findHighestEducation(userEducations);
        
        // Education presence score (0.0 to 1.0)
        double educationPresenceScore = 0.7; // Assuming having any education is good
        
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
        
        // Field relevance score (0.0 to 1.0)
        double fieldRelevanceScore = 0.5; // Default moderate score
        
        // Weighted score
        double weightedScore = (educationPresenceScore * 0.3 + degreeLevelScore * 0.4 + fieldRelevanceScore * 0.3);
        
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
     * Calculate cultural fit score
     * @param user Candidate user
     * @param job Job post
     * @return Cultural fit score with details
     */
    private CulturalFitScore calculateCulturalFitScore(User user, JobPost job) {
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
        
        // Weighted score
        double weightedScore = (locationScore * 0.6 + bioScore * 0.4);
        
        return new CulturalFitScore(weightedScore, locationScore, bioScore);
    }
    
    /**
     * Detect red flags in candidate profile
     * @param userExperiences User's experiences
     * @param userSkills User's skills
     * @return Red flag detection results
     */
    public RedFlagDetection detectRedFlags(List<Experience> userExperiences, List<Skill> userSkills) {
        List<String> redFlags = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check for employment gaps
        List<EmploymentGap> gaps = detectEmploymentGaps(userExperiences);
        for (EmploymentGap gap : gaps) {
            if (gap.getGapMonths() > 6) {
                redFlags.add("Employment gap of " + gap.getGapMonths() + " months from " + 
                           gap.getEndDate() + " to " + gap.getStartDate());
            } else if (gap.getGapMonths() > 3) {
                warnings.add("Employment gap of " + gap.getGapMonths() + " months from " + 
                           gap.getEndDate() + " to " + gap.getStartDate());
            }
        }
        
        // Check for skill inconsistencies
        List<String> inconsistencies = detectSkillInconsistencies(userExperiences, userSkills);
        redFlags.addAll(inconsistencies);
        
        // Check for overclaimed skills
        List<String> overclaimed = detectOverclaimedSkills(userSkills);
        warnings.addAll(overclaimed);
        
        boolean hasRedFlags = !redFlags.isEmpty();
        boolean hasWarnings = !warnings.isEmpty();
        
        return new RedFlagDetection(hasRedFlags, hasWarnings, redFlags, warnings);
    }
    
    /**
     * Detect employment gaps in work history
     * @param experiences User's experiences
     * @return List of employment gaps
     */
    private List<EmploymentGap> detectEmploymentGaps(List<Experience> experiences) {
        List<EmploymentGap> gaps = new ArrayList<>();
        
        // Sort experiences by start date
        List<Experience> sortedExperiences = experiences.stream()
                .filter(exp -> exp.getStartDate() != null)
                .sorted((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()))
                .collect(Collectors.toList());
        
        for (int i = 1; i < sortedExperiences.size(); i++) {
            Experience prev = sortedExperiences.get(i - 1);
            Experience current = sortedExperiences.get(i);
            
            // Check if there's a gap between previous job end and current job start
            if (prev.getEndDate() != null && current.getStartDate() != null) {
                long gapMonths = ChronoUnit.MONTHS.between(prev.getEndDate(), current.getStartDate());
                if (gapMonths > 1) { // More than 1 month gap
                    gaps.add(new EmploymentGap(prev.getEndDate(), current.getStartDate(), (int) gapMonths));
                }
            }
        }
        
        return gaps;
    }
    
    /**
     * Detect skill inconsistencies between experience and claimed skills
     * @param experiences User's experiences
     * @param skills User's claimed skills
     * @return List of inconsistencies
     */
    private List<String> detectSkillInconsistencies(List<Experience> experiences, List<Skill> skills) {
        List<String> inconsistencies = new ArrayList<>();
        
        // This is a simplified implementation
        // In a real implementation, this would use NLP to analyze job descriptions
        // and cross-reference with claimed skills
        
        if (experiences.isEmpty() && !skills.isEmpty()) {
            inconsistencies.add("Candidate claims skills but has no work experience");
        }
        
        return inconsistencies;
    }
    
    /**
     * Detect overclaimed skills (extremely high proficiency ratings)
     * @param skills User's skills
     * @return List of warnings
     */
    private List<String> detectOverclaimedSkills(List<Skill> skills) {
        List<String> warnings = new ArrayList<>();
        
        for (Skill skill : skills) {
            if (skill.getProficiencyLevel() > 9) {
                warnings.add("Extremely high proficiency claimed for skill: " + skill.getName() + " (" + 
                           skill.getProficiencyLevel() + "/10)");
            }
        }
        
        return warnings;
    }
    
    /**
     * Calculate weighted total score
     * @param skillScore Skill match score
     * @param experienceScore Experience match score
     * @param educationScore Education match score
     * @param culturalFitScore Cultural fit score
     * @param redFlags Red flag detection
     * @return Weighted total score
     */
    public double calculateWeightedTotalScore(
            SkillMatchScore skillScore,
            ExperienceMatchScore experienceScore,
            EducationMatchScore educationScore,
            CulturalFitScore culturalFitScore,
            RedFlagDetection redFlags) {
        
        // Base weighted score
        double weightedScore = (skillScore.getWeightedScore() * 0.4 +
                               experienceScore.getWeightedScore() * 0.3 +
                               educationScore.getWeightedScore() * 0.15 +
                               culturalFitScore.getWeightedScore() * 0.15);
        
        // Apply red flag penalties
        if (redFlags.isHasRedFlags()) {
            weightedScore *= 0.7; // 30% penalty for red flags
        } else if (redFlags.isHasWarnings()) {
            weightedScore *= 0.9; // 10% penalty for warnings
        }
        
        return Math.max(0.0, weightedScore); // Ensure non-negative score
    }
    
    // DTOs for detailed scoring
    
    public static class DetailedCandidateMatch {
        private User candidate;
        private DetailedCompatibilityScore score;
        
        public DetailedCandidateMatch(User candidate, DetailedCompatibilityScore score) {
            this.candidate = candidate;
            this.score = score;
        }
        
        public User getCandidate() { return candidate; }
        public void setCandidate(User candidate) { this.candidate = candidate; }
        
        public DetailedCompatibilityScore getScore() { return score; }
        public void setScore(DetailedCompatibilityScore score) { this.score = score; }
    }
    
    public static class DetailedCompatibilityScore {
        private double totalScore;
        private SkillMatchScore skillScore;
        private ExperienceMatchScore experienceScore;
        private EducationMatchScore educationScore;
        private CulturalFitScore culturalFitScore;
        private RedFlagDetection redFlags;
        
        public DetailedCompatibilityScore(
                double totalScore,
                SkillMatchScore skillScore,
                ExperienceMatchScore experienceScore,
                EducationMatchScore educationScore,
                CulturalFitScore culturalFitScore,
                RedFlagDetection redFlags) {
            this.totalScore = totalScore;
            this.skillScore = skillScore;
            this.experienceScore = experienceScore;
            this.educationScore = educationScore;
            this.culturalFitScore = culturalFitScore;
            this.redFlags = redFlags;
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
        
        public RedFlagDetection getRedFlags() { return redFlags; }
        public void setRedFlags(RedFlagDetection redFlags) { this.redFlags = redFlags; }
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
    
    public static class RedFlagDetection {
        private boolean hasRedFlags;
        private boolean hasWarnings;
        private List<String> redFlags;
        private List<String> warnings;
        
        public RedFlagDetection(boolean hasRedFlags, boolean hasWarnings, List<String> redFlags, List<String> warnings) {
            this.hasRedFlags = hasRedFlags;
            this.hasWarnings = hasWarnings;
            this.redFlags = redFlags;
            this.warnings = warnings;
        }
        
        public boolean isHasRedFlags() { return hasRedFlags; }
        public void setHasRedFlags(boolean hasRedFlags) { this.hasRedFlags = hasRedFlags; }
        
        public boolean isHasWarnings() { return hasWarnings; }
        public void setHasWarnings(boolean hasWarnings) { this.hasWarnings = hasWarnings; }
        
        public List<String> getRedFlags() { return redFlags; }
        public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
    
    public static class EmploymentGap {
        private LocalDate endDate;
        private LocalDate startDate;
        private int gapMonths;
        
        public EmploymentGap(LocalDate endDate, LocalDate startDate, int gapMonths) {
            this.endDate = endDate;
            this.startDate = startDate;
            this.gapMonths = gapMonths;
        }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public int getGapMonths() { return gapMonths; }
        public void setGapMonths(int gapMonths) { this.gapMonths = gapMonths; }
    }
}