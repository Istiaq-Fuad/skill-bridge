package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobDescriptionGeneratorService {

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generate a comprehensive job description using AI
     * 
     * @param request  Job description generation request
     * @param employer The employer requesting the job description
     * @return Generated job description
     */
    public JobDescriptionResult generateJobDescription(JobDescriptionRequest request, User employer) {
        try {
            String prompt = buildJobDescriptionPrompt(request, employer);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Job Description Generation");

            return parseJobDescriptionResponse(aiResponse.getContent(), request);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate job description: " + e.getMessage(), e);
        }
    }

    /**
     * Generate job description from basic requirements
     * 
     * @param jobTitle        The job title
     * @param company         Company name
     * @param department      Department name
     * @param experienceLevel Experience level required
     * @param skills          Required skills
     * @param employer        The employer
     * @return Generated job description
     */
    public JobDescriptionResult generateBasicJobDescription(String jobTitle, String company,
            String department, String experienceLevel,
            List<String> skills, User employer) {
        JobDescriptionRequest request = new JobDescriptionRequest();
        request.setJobTitle(jobTitle);
        request.setCompany(company);
        request.setDepartment(department);
        request.setExperienceLevel(experienceLevel);
        request.setRequiredSkills(skills);

        return generateJobDescription(request, employer);
    }

    /**
     * Enhance an existing job description
     * 
     * @param existingDescription Existing job description text
     * @param improvements        List of improvements to make
     * @param employer            The employer
     * @return Enhanced job description
     */
    public JobDescriptionResult enhanceJobDescription(String existingDescription,
            List<String> improvements, User employer) {
        try {
            String prompt = buildEnhancementPrompt(existingDescription, improvements);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Job Description Enhancement");

            JobDescriptionResult result = parseJobDescriptionResponse(aiResponse.getContent(), null);
            result.setEnhanced(true);
            result.setOriginalDescription(existingDescription);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to enhance job description: " + e.getMessage(), e);
        }
    }

    /**
     * Generate multiple job description variations
     * 
     * @param request    Base job description request
     * @param variations Number of variations to generate
     * @param employer   The employer
     * @return List of job description variations
     */
    public List<JobDescriptionResult> generateJobDescriptionVariations(JobDescriptionRequest request,
            int variations, User employer) {
        // Implementation for generating multiple variations
        // This would involve multiple AI calls with slightly different prompts
        throw new RuntimeException("Job description variations feature coming soon!");
    }

    private String buildJobDescriptionPrompt(JobDescriptionRequest request, User employer) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a comprehensive, professional job description with the following structure:\n\n");
        prompt.append("Company: ")
                .append(request.getCompany() != null ? request.getCompany() : employer.getCompanyName()).append("\n");
        prompt.append("Job Title: ").append(request.getJobTitle()).append("\n");

        if (request.getDepartment() != null) {
            prompt.append("Department: ").append(request.getDepartment()).append("\n");
        }

        prompt.append("\nPlease include:\n");
        prompt.append("1. Company Overview (if company description provided)\n");
        prompt.append("2. Job Summary\n");
        prompt.append("3. Key Responsibilities (5-8 bullet points)\n");
        prompt.append("4. Required Qualifications\n");
        prompt.append("5. Preferred Qualifications\n");
        prompt.append("6. Benefits and Perks\n");
        prompt.append("7. Application Instructions\n\n");

        // Add specific requirements
        if (request.getExperienceLevel() != null) {
            prompt.append("Experience Level: ").append(request.getExperienceLevel()).append("\n");
        }

        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            prompt.append("Required Skills: ").append(String.join(", ", request.getRequiredSkills())).append("\n");
        }

        if (request.getPreferredSkills() != null && !request.getPreferredSkills().isEmpty()) {
            prompt.append("Preferred Skills: ").append(String.join(", ", request.getPreferredSkills())).append("\n");
        }

        if (request.getJobType() != null) {
            prompt.append("Job Type: ").append(request.getJobType()).append("\n");
        }

        if (request.getWorkLocation() != null) {
            prompt.append("Work Location: ").append(request.getWorkLocation()).append("\n");
        }

        if (request.getSalaryRange() != null) {
            prompt.append("Salary Range: ").append(request.getSalaryRange()).append("\n");
        }

        if (request.getAdditionalRequirements() != null) {
            prompt.append("Additional Requirements: ").append(request.getAdditionalRequirements()).append("\n");
        }

        // Add company context if available
        if (employer.getCompanyDescription() != null) {
            prompt.append("\nCompany Description: ").append(employer.getCompanyDescription()).append("\n");
        }

        prompt.append(
                "\nMake the job description engaging, professional, and tailored to attract qualified candidates.");
        prompt.append(" Use industry-standard language and include relevant keywords for ATS optimization.");

        return prompt.toString();
    }

    private String buildEnhancementPrompt(String existingDescription, List<String> improvements) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Please enhance the following job description by incorporating these improvements:\n\n");

        if (improvements != null && !improvements.isEmpty()) {
            prompt.append("Requested Improvements:\n");
            for (String improvement : improvements) {
                prompt.append("- ").append(improvement).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Original Job Description:\n");
        prompt.append(existingDescription).append("\n\n");

        prompt.append("Please provide an enhanced version that:\n");
        prompt.append("- Incorporates the requested improvements\n");
        prompt.append("- Maintains professional tone\n");
        prompt.append("- Improves clarity and attractiveness\n");
        prompt.append("- Optimizes for ATS systems\n");
        prompt.append("- Keeps the original structure but enhances content\n");

        return prompt.toString();
    }

    private JobDescriptionResult parseJobDescriptionResponse(String aiResponse, JobDescriptionRequest originalRequest) {
        JobDescriptionResult result = new JobDescriptionResult();
        result.setGeneratedDescription(aiResponse.trim());
        result.setOriginalRequest(originalRequest);
        result.setGeneratedAt(new java.util.Date());

        // Extract sections from the response (basic implementation)
        result.setSections(extractSections(aiResponse));

        return result;
    }

    private Map<String, String> extractSections(String description) {
        Map<String, String> sections = new HashMap<>();

        // This is a basic implementation - you could enhance this with better parsing
        String[] lines = description.split("\n");
        StringBuilder currentSection = new StringBuilder();
        String currentSectionTitle = null;

        for (String line : lines) {
            line = line.trim();

            // Check if line is a section header (contains keywords and ends with colon)
            if (line.toLowerCase().contains("overview") ||
                    line.toLowerCase().contains("summary") ||
                    line.toLowerCase().contains("responsibilities") ||
                    line.toLowerCase().contains("qualifications") ||
                    line.toLowerCase().contains("requirements") ||
                    line.toLowerCase().contains("benefits") ||
                    line.toLowerCase().contains("application")) {

                // Save previous section
                if (currentSectionTitle != null && currentSection.length() > 0) {
                    sections.put(currentSectionTitle, currentSection.toString().trim());
                }

                // Start new section
                currentSectionTitle = line.replaceAll("[^a-zA-Z\\s]", "").trim();
                currentSection = new StringBuilder();
            } else if (!line.isEmpty()) {
                currentSection.append(line).append("\n");
            }
        }

        // Save last section
        if (currentSectionTitle != null && currentSection.length() > 0) {
            sections.put(currentSectionTitle, currentSection.toString().trim());
        }

        return sections;
    }

    /**
     * Request class for job description generation
     */
    public static class JobDescriptionRequest {
        private String jobTitle;
        private String company;
        private String department;
        private String experienceLevel;
        private String jobType; // Full-time, Part-time, Contract, etc.
        private String workLocation; // Remote, On-site, Hybrid
        private String salaryRange;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private String additionalRequirements;
        private String industry;
        private String teamSize;

        // Getters and Setters
        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public void setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
        }

        public String getJobType() {
            return jobType;
        }

        public void setJobType(String jobType) {
            this.jobType = jobType;
        }

        public String getWorkLocation() {
            return workLocation;
        }

        public void setWorkLocation(String workLocation) {
            this.workLocation = workLocation;
        }

        public String getSalaryRange() {
            return salaryRange;
        }

        public void setSalaryRange(String salaryRange) {
            this.salaryRange = salaryRange;
        }

        public List<String> getRequiredSkills() {
            return requiredSkills;
        }

        public void setRequiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
        }

        public List<String> getPreferredSkills() {
            return preferredSkills;
        }

        public void setPreferredSkills(List<String> preferredSkills) {
            this.preferredSkills = preferredSkills;
        }

        public String getAdditionalRequirements() {
            return additionalRequirements;
        }

        public void setAdditionalRequirements(String additionalRequirements) {
            this.additionalRequirements = additionalRequirements;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getTeamSize() {
            return teamSize;
        }

        public void setTeamSize(String teamSize) {
            this.teamSize = teamSize;
        }
    }

    /**
     * Result class for job description generation
     */
    public static class JobDescriptionResult {
        private String generatedDescription;
        private JobDescriptionRequest originalRequest;
        private java.util.Date generatedAt;
        private Map<String, String> sections;
        private boolean enhanced;
        private String originalDescription;
        private double qualityScore;

        // Getters and Setters
        public String getGeneratedDescription() {
            return generatedDescription;
        }

        public void setGeneratedDescription(String generatedDescription) {
            this.generatedDescription = generatedDescription;
        }

        public JobDescriptionRequest getOriginalRequest() {
            return originalRequest;
        }

        public void setOriginalRequest(JobDescriptionRequest originalRequest) {
            this.originalRequest = originalRequest;
        }

        public java.util.Date getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(java.util.Date generatedAt) {
            this.generatedAt = generatedAt;
        }

        public Map<String, String> getSections() {
            return sections;
        }

        public void setSections(Map<String, String> sections) {
            this.sections = sections;
        }

        public boolean isEnhanced() {
            return enhanced;
        }

        public void setEnhanced(boolean enhanced) {
            this.enhanced = enhanced;
        }

        public String getOriginalDescription() {
            return originalDescription;
        }

        public void setOriginalDescription(String originalDescription) {
            this.originalDescription = originalDescription;
        }

        public double getQualityScore() {
            return qualityScore;
        }

        public void setQualityScore(double qualityScore) {
            this.qualityScore = qualityScore;
        }
    }
}