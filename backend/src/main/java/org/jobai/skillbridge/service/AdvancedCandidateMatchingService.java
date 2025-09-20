package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvancedCandidateMatchingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private ResumeParsingService resumeParsingService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Find best matching candidates for a job posting
     * 
     * @param jobPost    The job posting
     * @param maxResults Maximum number of candidates to return
     * @return List of ranked candidate matches
     */
    public List<CandidateMatchResult> findBestCandidates(JobPost jobPost, int maxResults) {
        // Get all job seekers who haven't applied to this job yet
        List<User> jobSeekers = getAvailableJobSeekers(jobPost);

        List<CandidateMatchResult> matchResults = new ArrayList<>();

        for (User candidate : jobSeekers) {
            CandidateMatchResult matchResult = evaluateCandidate(candidate, jobPost);
            if (matchResult.getOverallScore() > 0.3) { // Only include candidates with reasonable match
                matchResults.add(matchResult);
            }
        }

        // Sort by overall score descending and limit results
        return matchResults.stream()
                .sorted((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Evaluate a single candidate against a job posting
     * 
     * @param candidate The candidate to evaluate
     * @param jobPost   The job posting
     * @return Detailed match evaluation
     */
    public CandidateMatchResult evaluateCandidate(User candidate, JobPost jobPost) {
        CandidateMatchResult result = new CandidateMatchResult();
        result.setCandidate(candidate);
        result.setJobPost(jobPost);
        result.setEvaluatedAt(new Date());

        // Calculate individual scores
        double skillsScore = calculateSkillsMatch(candidate, jobPost);
        double experienceScore = calculateExperienceMatch(candidate, jobPost);
        double educationScore = calculateEducationMatch(candidate, jobPost);
        double locationScore = calculateLocationMatch(candidate, jobPost);

        // AI-powered semantic matching
        double aiScore = calculateAISemanticMatch(candidate, jobPost);

        // Set individual scores
        result.setSkillsScore(skillsScore);
        result.setExperienceScore(experienceScore);
        result.setEducationScore(educationScore);
        result.setLocationScore(locationScore);
        result.setAiSemanticScore(aiScore);

        // Calculate weighted overall score
        double overallScore = calculateOverallScore(skillsScore, experienceScore,
                educationScore, locationScore, aiScore);
        result.setOverallScore(overallScore);

        // Generate matching reasons
        result.setMatchingReasons(generateMatchingReasons(result, candidate, jobPost));
        result.setMismatchReasons(generateMismatchReasons(result, candidate, jobPost));

        return result;
    }

    /**
     * Get job recommendations for a candidate
     * 
     * @param candidate  The candidate
     * @param maxResults Maximum number of jobs to return
     * @return List of recommended jobs
     */
    public List<JobMatchResult> getJobRecommendations(User candidate, int maxResults) {
        // Get all available job postings
        List<JobPost> availableJobs = getAvailableJobs(candidate);

        List<JobMatchResult> jobMatches = new ArrayList<>();

        for (JobPost job : availableJobs) {
            CandidateMatchResult candidateMatch = evaluateCandidate(candidate, job);

            JobMatchResult jobMatch = new JobMatchResult();
            jobMatch.setJob(job);
            jobMatch.setMatchScore(candidateMatch.getOverallScore());
            jobMatch.setMatchingReasons(candidateMatch.getMatchingReasons());
            jobMatch.setRecommendedAt(new Date());

            if (jobMatch.getMatchScore() > 0.4) { // Only recommend reasonably good matches
                jobMatches.add(jobMatch);
            }
        }

        // Sort by match score descending and limit results
        return jobMatches.stream()
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private List<User> getAvailableJobSeekers(JobPost jobPost) {
        // Get all job seekers
        List<User> allJobSeekers = userRepository.findByRole(UserRole.JOB_SEEKER);

        // Get users who already applied to this job
        List<JobApplication> existingApplications = jobApplicationRepository.findByJobPost(jobPost);
        Set<Long> appliedUserIds = existingApplications.stream()
                .map(app -> app.getUser().getId())
                .collect(Collectors.toSet());

        // Return job seekers who haven't applied yet
        return allJobSeekers.stream()
                .filter(user -> !appliedUserIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private List<JobPost> getAvailableJobs(User candidate) {
        // This would typically get jobs from JobRepository - for now using a
        // placeholder
        // You would inject JobRepository and use it here
        return new ArrayList<>(); // Placeholder
    }

    private double calculateSkillsMatch(User candidate, JobPost jobPost) {
        List<Skill> candidateSkills = skillRepository.findByUser(candidate);

        if (candidateSkills.isEmpty()) {
            return 0.0;
        }

        String jobRequiredSkills = extractRequiredSkills(jobPost);
        if (jobRequiredSkills == null || jobRequiredSkills.isEmpty()) {
            return 0.5; // Neutral score if job doesn't specify skills
        }

        Set<String> jobSkillsSet = Arrays.stream(jobRequiredSkills.toLowerCase().split("[,;\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> candidateSkillsSet = candidateSkills.stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(candidateSkillsSet);
        intersection.retainAll(jobSkillsSet);

        Set<String> union = new HashSet<>(candidateSkillsSet);
        union.addAll(jobSkillsSet);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private double calculateExperienceMatch(User candidate, JobPost jobPost) {
        List<Experience> candidateExperience = experienceRepository.findByUser(candidate);

        if (candidateExperience.isEmpty()) {
            return 0.2; // Low score for no experience
        }

        // Calculate total years of experience
        int totalYearsOfExperience = candidateExperience.stream()
                .mapToInt(exp -> calculateYearsOfExperience(exp))
                .sum();

        // Extract required experience from job description
        int requiredExperience = extractRequiredExperience(jobPost);

        if (requiredExperience == 0) {
            return 0.7; // Default score if no specific requirement
        }

        // Score based on experience match
        if (totalYearsOfExperience >= requiredExperience) {
            return Math.min(1.0, 0.8 + (totalYearsOfExperience - requiredExperience) * 0.05);
        } else {
            return Math.max(0.1, (double) totalYearsOfExperience / requiredExperience * 0.8);
        }
    }

    private double calculateEducationMatch(User candidate, JobPost jobPost) {
        List<Education> candidateEducation = educationRepository.findByUser(candidate);

        if (candidateEducation.isEmpty()) {
            return 0.3; // Low score for no education
        }

        // Simple education matching - can be enhanced
        String requiredEducation = extractRequiredEducation(jobPost);

        if (requiredEducation == null || requiredEducation.isEmpty()) {
            return 0.7; // Neutral score if no specific requirement
        }

        // Check for degree level matches
        boolean hasBachelor = candidateEducation.stream()
                .anyMatch(edu -> edu.getDegree().toLowerCase().contains("bachelor"));
        boolean hasMaster = candidateEducation.stream()
                .anyMatch(edu -> edu.getDegree().toLowerCase().contains("master"));
        boolean hasPhD = candidateEducation.stream()
                .anyMatch(edu -> edu.getDegree().toLowerCase().contains("phd") ||
                        edu.getDegree().toLowerCase().contains("doctorate"));

        String reqLower = requiredEducation.toLowerCase();

        if (reqLower.contains("phd") || reqLower.contains("doctorate")) {
            return hasPhD ? 1.0 : (hasMaster ? 0.7 : (hasBachelor ? 0.5 : 0.2));
        } else if (reqLower.contains("master")) {
            return hasMaster || hasPhD ? 1.0 : (hasBachelor ? 0.7 : 0.3);
        } else if (reqLower.contains("bachelor")) {
            return hasBachelor || hasMaster || hasPhD ? 1.0 : 0.4;
        }

        return 0.6; // Default for other education requirements
    }

    private double calculateLocationMatch(User candidate, JobPost jobPost) {
        String candidateLocation = candidate.getCity();
        String jobLocation = jobPost.getLocation();

        if (candidateLocation == null || jobLocation == null) {
            return 0.5; // Neutral score if location info is missing
        }

        // Check for remote work options
        if (jobLocation.toLowerCase().contains("remote")) {
            return 1.0;
        }

        // Simple location matching - can be enhanced with geography APIs
        if (candidateLocation.equalsIgnoreCase(jobLocation)) {
            return 1.0;
        }

        // Check if same state/region (basic implementation)
        String[] candidateParts = candidateLocation.split(",");
        String[] jobParts = jobLocation.split(",");

        if (candidateParts.length > 1 && jobParts.length > 1) {
            String candidateRegion = candidateParts[candidateParts.length - 1].trim();
            String jobRegion = jobParts[jobParts.length - 1].trim();
            if (candidateRegion.equalsIgnoreCase(jobRegion)) {
                return 0.7;
            }
        }

        return 0.2; // Different locations
    }

    private double calculateAISemanticMatch(User candidate, JobPost jobPost) {
        try {
            String candidateProfile = buildCandidateProfile(candidate);
            String jobDescription = jobPost.getDescription();

            String prompt = String.format(
                    "Analyze the semantic match between this job description and candidate profile. " +
                            "Rate the compatibility on a scale of 0.0 to 1.0 based on skills alignment, " +
                            "experience relevance, and overall fit. Return only the numeric score.\n\n" +
                            "Job Description:\n%s\n\nCandidate Profile:\n%s\n\n" +
                            "Compatibility Score (0.0-1.0):",
                    jobDescription, candidateProfile);

            AiResponseDto response = mistralAiService.generateText(prompt, "Candidate Matching");
            String content = response.getContent().trim();

            // Extract numeric score from response
            try {
                return Double.parseDouble(content.replaceAll("[^0-9.]", ""));
            } catch (NumberFormatException e) {
                return 0.5; // Default score if parsing fails
            }

        } catch (Exception e) {
            return 0.5; // Default score if AI call fails
        }
    }

    private double calculateOverallScore(double skillsScore, double experienceScore,
            double educationScore, double locationScore, double aiScore) {
        // Weighted average - you can adjust these weights based on importance
        double skillsWeight = 0.35;
        double experienceWeight = 0.25;
        double educationWeight = 0.15;
        double locationWeight = 0.10;
        double aiWeight = 0.15;

        return (skillsScore * skillsWeight) +
                (experienceScore * experienceWeight) +
                (educationScore * educationWeight) +
                (locationScore * locationWeight) +
                (aiScore * aiWeight);
    }

    private List<String> generateMatchingReasons(CandidateMatchResult result, User candidate, JobPost jobPost) {
        List<String> reasons = new ArrayList<>();

        if (result.getSkillsScore() > 0.7) {
            reasons.add("Strong skills alignment with job requirements");
        }
        if (result.getExperienceScore() > 0.8) {
            reasons.add("Extensive relevant work experience");
        }
        if (result.getEducationScore() > 0.8) {
            reasons.add("Educational background matches requirements");
        }
        if (result.getLocationScore() > 0.9) {
            reasons.add("Perfect location match or remote-friendly");
        }
        if (result.getAiSemanticScore() > 0.7) {
            reasons.add("AI analysis shows strong semantic compatibility");
        }

        return reasons;
    }

    private List<String> generateMismatchReasons(CandidateMatchResult result, User candidate, JobPost jobPost) {
        List<String> reasons = new ArrayList<>();

        if (result.getSkillsScore() < 0.3) {
            reasons.add("Limited skills match with job requirements");
        }
        if (result.getExperienceScore() < 0.4) {
            reasons.add("Insufficient relevant work experience");
        }
        if (result.getEducationScore() < 0.4) {
            reasons.add("Educational background doesn't align with requirements");
        }
        if (result.getLocationScore() < 0.3) {
            reasons.add("Location mismatch may require relocation");
        }

        return reasons;
    }

    // Helper methods
    private String extractRequiredSkills(JobPost jobPost) {
        String description = jobPost.getDescription();
        if (description == null)
            return "";

        // Simple extraction - look for skills section
        String lower = description.toLowerCase();
        int skillsIndex = lower.indexOf("skills");
        if (skillsIndex != -1) {
            // Try to extract next paragraph or bullet points
            return description.substring(skillsIndex, Math.min(description.length(), skillsIndex + 500));
        }

        return ""; // Fallback - JobPost doesn't have structured requiredSkills field
    }

    private int extractRequiredExperience(JobPost jobPost) {
        String description = jobPost.getDescription();
        if (description == null)
            return 0;

        // Look for experience requirements like "3+ years", "5 years", etc.
        String[] patterns = { "(\\d+)\\+?\\s*years?", "(\\d+)\\+?\\s*yrs?" };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern,
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(description);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return 0; // No specific experience requirement found
    }

    private String extractRequiredEducation(JobPost jobPost) {
        String description = jobPost.getDescription();
        if (description == null)
            return "";

        // Look for education section
        String lower = description.toLowerCase();
        int educationIndex = Math.max(
                lower.indexOf("education"),
                Math.max(lower.indexOf("degree"), lower.indexOf("qualification")));

        if (educationIndex != -1) {
            return description.substring(educationIndex, Math.min(description.length(), educationIndex + 300));
        }

        return "";
    }

    private int calculateYearsOfExperience(Experience experience) {
        // Simple calculation - you might want to enhance this
        if (experience.getStartDate() != null && experience.getEndDate() != null) {
            return experience.getEndDate().getYear() - experience.getStartDate().getYear();
        }
        return 1; // Default assumption
    }

    private String buildCandidateProfile(User candidate) {
        StringBuilder profile = new StringBuilder();

        profile.append("Name: ").append(candidate.getFirstName()).append(" ").append(candidate.getLastName())
                .append("\n");

        if (candidate.getBio() != null) {
            profile.append("Summary: ").append(candidate.getBio()).append("\n");
        }

        // Add skills
        List<Skill> skills = skillRepository.findByUser(candidate);
        if (!skills.isEmpty()) {
            profile.append("Skills: ");
            profile.append(skills.stream().map(Skill::getName).collect(Collectors.joining(", ")));
            profile.append("\n");
        }

        // Add experience
        List<Experience> experiences = experienceRepository.findByUser(candidate);
        if (!experiences.isEmpty()) {
            profile.append("Experience:\n");
            for (Experience exp : experiences) {
                profile.append("- ").append(exp.getPosition()).append(" at ").append(exp.getCompany()).append("\n");
                if (exp.getDescription() != null) {
                    profile.append("  ").append(exp.getDescription()).append("\n");
                }
            }
        }

        return profile.toString();
    }

    /**
     * Result classes
     */
    public static class CandidateMatchResult {
        private User candidate;
        private JobPost jobPost;
        private double overallScore;
        private double skillsScore;
        private double experienceScore;
        private double educationScore;
        private double locationScore;
        private double aiSemanticScore;
        private List<String> matchingReasons;
        private List<String> mismatchReasons;
        private Date evaluatedAt;

        // Getters and Setters
        public User getCandidate() {
            return candidate;
        }

        public void setCandidate(User candidate) {
            this.candidate = candidate;
        }

        public JobPost getJobPost() {
            return jobPost;
        }

        public void setJobPost(JobPost jobPost) {
            this.jobPost = jobPost;
        }

        public double getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(double overallScore) {
            this.overallScore = overallScore;
        }

        public double getSkillsScore() {
            return skillsScore;
        }

        public void setSkillsScore(double skillsScore) {
            this.skillsScore = skillsScore;
        }

        public double getExperienceScore() {
            return experienceScore;
        }

        public void setExperienceScore(double experienceScore) {
            this.experienceScore = experienceScore;
        }

        public double getEducationScore() {
            return educationScore;
        }

        public void setEducationScore(double educationScore) {
            this.educationScore = educationScore;
        }

        public double getLocationScore() {
            return locationScore;
        }

        public void setLocationScore(double locationScore) {
            this.locationScore = locationScore;
        }

        public double getAiSemanticScore() {
            return aiSemanticScore;
        }

        public void setAiSemanticScore(double aiSemanticScore) {
            this.aiSemanticScore = aiSemanticScore;
        }

        public List<String> getMatchingReasons() {
            return matchingReasons;
        }

        public void setMatchingReasons(List<String> matchingReasons) {
            this.matchingReasons = matchingReasons;
        }

        public List<String> getMismatchReasons() {
            return mismatchReasons;
        }

        public void setMismatchReasons(List<String> mismatchReasons) {
            this.mismatchReasons = mismatchReasons;
        }

        public Date getEvaluatedAt() {
            return evaluatedAt;
        }

        public void setEvaluatedAt(Date evaluatedAt) {
            this.evaluatedAt = evaluatedAt;
        }
    }

    public static class JobMatchResult {
        private JobPost job;
        private double matchScore;
        private List<String> matchingReasons;
        private Date recommendedAt;

        // Getters and Setters
        public JobPost getJob() {
            return job;
        }

        public void setJob(JobPost job) {
            this.job = job;
        }

        public double getMatchScore() {
            return matchScore;
        }

        public void setMatchScore(double matchScore) {
            this.matchScore = matchScore;
        }

        public List<String> getMatchingReasons() {
            return matchingReasons;
        }

        public void setMatchingReasons(List<String> matchingReasons) {
            this.matchingReasons = matchingReasons;
        }

        public Date getRecommendedAt() {
            return recommendedAt;
        }

        public void setRecommendedAt(Date recommendedAt) {
            this.recommendedAt = recommendedAt;
        }
    }
}