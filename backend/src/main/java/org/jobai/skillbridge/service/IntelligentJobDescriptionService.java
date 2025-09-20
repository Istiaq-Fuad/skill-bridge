package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Iterator;

@Service
public class IntelligentJobDescriptionService {

    @Autowired
    private MistralAiService mistralAiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generate an optimized job description based on minimal input
     * 
     * @param jobTitle        The job title
     * @param industry        The industry sector
     * @param experienceLevel Experience level (junior, mid, senior, lead)
     * @param location        Job location
     * @return Optimized job description
     */
    public JobDescriptionGenerationResult generateJobDescription(
            String jobTitle,
            String industry,
            String experienceLevel,
            String location) {

        long startTime = System.currentTimeMillis();

        try {
            // Validate inputs
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                throw new IllegalArgumentException("Job title cannot be empty");
            }

            // Create context for AI generation
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", jobTitle);
            context.put("industry", industry != null ? industry : "technology");
            context.put("experience_level", experienceLevel != null ? experienceLevel : "mid");
            context.put("location", location != null ? location : "remote");

            // Generate optimized job description
            String prompt = buildJobDescriptionPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Generate an optimized job description");

            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to generate job description: " + aiResponse.getMessage());
            }

            // Parse the AI response to extract structured data
            JobDescriptionGenerationResult result = parseJobDescriptionResponse(aiResponse.getContent());
            result.setJobTitle(jobTitle);
            result.setIndustry(industry);
            result.setExperienceLevel(experienceLevel);
            result.setLocation(location);

            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating job description: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();

            throw new AiServiceException(errorMessage, e);
        }
    }

    /**
     * Optimize an existing job description
     * 
     * @param existingJob The existing job post
     * @return Optimized job description
     */
    public JobDescriptionOptimizationResult optimizeJobDescription(JobPost existingJob) {
        long startTime = System.currentTimeMillis();

        try {
            // Validate input
            if (existingJob == null) {
                throw new IllegalArgumentException("Existing job cannot be null");
            }

            // Create context for AI optimization
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", existingJob.getTitle());
            context.put("job_description", existingJob.getDescription());
            context.put("company", existingJob.getCompany());
            context.put("location", existingJob.getLocation());
            context.put("requirements", existingJob.getRequirements());

            // Generate optimization suggestions
            String prompt = buildOptimizationPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Optimize job description");

            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to optimize job description: " + aiResponse.getMessage());
            }

            // Parse the AI response
            JobDescriptionOptimizationResult result = parseOptimizationResponse(aiResponse.getContent());

            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error optimizing job description: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();

            throw new AiServiceException(errorMessage, e);
        }
    }

    /**
     * Suggest relevant skills for a job
     * 
     * @param jobTitle        Job title
     * @param industry        Industry
     * @param experienceLevel Experience level
     * @return Skill suggestions
     */
    public SkillSuggestionResult suggestSkills(String jobTitle, String industry, String experienceLevel) {
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", jobTitle);
            context.put("industry", industry);
            context.put("experience_level", experienceLevel);

            String prompt = buildSkillSuggestionPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Suggest relevant skills");

            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to suggest skills: " + aiResponse.getMessage());
            }

            SkillSuggestionResult result = parseSkillSuggestionResponse(aiResponse.getContent());

            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error suggesting skills: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();

            throw new AiServiceException(errorMessage, e);
        }
    }

    /**
     * Suggest competitive salary ranges
     * 
     * @param jobTitle        Job title
     * @param industry        Industry
     * @param experienceLevel Experience level
     * @param location        Location
     * @return Salary suggestions
     */
    public SalarySuggestionResult suggestSalaryRanges(String jobTitle, String industry, String experienceLevel,
            String location) {
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", jobTitle);
            context.put("industry", industry);
            context.put("experience_level", experienceLevel);
            context.put("location", location);

            String prompt = buildSalarySuggestionPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Suggest salary ranges");

            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to suggest salary ranges: " + aiResponse.getMessage());
            }

            SalarySuggestionResult result = parseSalarySuggestionResponse(aiResponse.getContent());

            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);

            return result;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error suggesting salary ranges: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();

            throw new AiServiceException(errorMessage, e);
        }
    }

    // Private helper methods for building prompts
    private String buildJobDescriptionPrompt(Map<String, Object> context) {
        return String.format(
                "Generate a comprehensive and attractive job description for the following role:\n\n" +
                        "Job Title: %s\n" +
                        "Industry: %s\n" +
                        "Experience Level: %s\n" +
                        "Location: %s\n\n" +
                        "Please provide a structured response with the following sections in JSON format:\n" +
                        "{\n" +
                        "  \"companyOverview\": \"Brief company description\",\n" +
                        "  \"jobSummary\": \"Engaging job summary\",\n" +
                        "  \"responsibilities\": [\"List of key responsibilities\"],\n" +
                        "  \"requiredQualifications\": [\"Must-have qualifications\"],\n" +
                        "  \"preferredQualifications\": [\"Nice-to-have qualifications\"],\n" +
                        "  \"benefits\": [\"Employee benefits and perks\"],\n" +
                        "  \"salaryRange\": \"Competitive salary range\"\n" +
                        "}",
                context.get("job_title"),
                context.get("industry"),
                context.get("experience_level"),
                context.get("location"));
    }

    private String buildOptimizationPrompt(Map<String, Object> context) {
        return String.format(
                "Analyze and optimize the following job description:\n\n" +
                        "Title: %s\n" +
                        "Description: %s\n" +
                        "Company: %s\n" +
                        "Location: %s\n\n" +
                        "Provide optimization suggestions in JSON format:\n" +
                        "{\n" +
                        "  \"optimizedTitle\": \"Improved job title\",\n" +
                        "  \"optimizedDescription\": \"Enhanced job description\",\n" +
                        "  \"improvements\": [\"List of specific improvements made\"],\n" +
                        "  \"seoKeywords\": [\"SEO-friendly keywords to include\"],\n" +
                        "  \"attractivenessScore\": \"Score out of 10 for job attractiveness\"\n" +
                        "}",
                context.get("job_title"),
                context.get("job_description"),
                context.get("company"),
                context.get("location"));
    }

    private String buildSkillSuggestionPrompt(Map<String, Object> context) {
        return String.format(
                "Suggest relevant skills for the following job:\n\n" +
                        "Job Title: %s\n" +
                        "Industry: %s\n" +
                        "Experience Level: %s\n\n" +
                        "Provide skill suggestions in JSON format:\n" +
                        "{\n" +
                        "  \"technicalSkills\": [\"List of technical skills\"],\n" +
                        "  \"softSkills\": [\"List of soft skills\"],\n" +
                        "  \"tools\": [\"Relevant tools and technologies\"],\n" +
                        "  \"certifications\": [\"Valuable certifications\"]\n" +
                        "}",
                context.get("job_title"),
                context.get("industry"),
                context.get("experience_level"));
    }

    private String buildSalarySuggestionPrompt(Map<String, Object> context) {
        return String.format(
                "Suggest competitive salary ranges for the following job:\n\n" +
                        "Job Title: %s\n" +
                        "Industry: %s\n" +
                        "Experience Level: %s\n" +
                        "Location: %s\n\n" +
                        "Provide salary suggestions in JSON format:\n" +
                        "{\n" +
                        "  \"currency\": \"USD\",\n" +
                        "  \"minSalary\": \"Minimum salary amount\",\n" +
                        "  \"maxSalary\": \"Maximum salary amount\",\n" +
                        "  \"averageSalary\": \"Average market salary\",\n" +
                        "  \"factors\": [\"Factors affecting salary range\"]\n" +
                        "}",
                context.get("job_title"),
                context.get("industry"),
                context.get("experience_level"),
                context.get("location"));
    }

    // Private helper methods for parsing responses
    private JobDescriptionGenerationResult parseJobDescriptionResponse(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            JobDescriptionGenerationResult result = new JobDescriptionGenerationResult();

            result.setCompanyOverview(jsonNode.path("companyOverview").asText());
            result.setJobSummary(jsonNode.path("jobSummary").asText());
            result.setSalaryRange(jsonNode.path("salaryRange").asText());

            // Parse arrays
            result.setResponsibilities(parseJsonArray(jsonNode.path("responsibilities")));
            result.setRequiredQualifications(parseJsonArray(jsonNode.path("requiredQualifications")));
            result.setPreferredQualifications(parseJsonArray(jsonNode.path("preferredQualifications")));
            result.setBenefits(parseJsonArray(jsonNode.path("benefits")));

            return result;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse job description response: " + e.getMessage());
        }
    }

    private JobDescriptionOptimizationResult parseOptimizationResponse(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            JobDescriptionOptimizationResult result = new JobDescriptionOptimizationResult();

            result.setOptimizedTitle(jsonNode.path("optimizedTitle").asText());
            result.setOptimizedDescription(jsonNode.path("optimizedDescription").asText());
            result.setAttractivenessScore(jsonNode.path("attractivenessScore").asInt());

            result.setImprovements(parseJsonArray(jsonNode.path("improvements")));
            result.setSeoKeywords(parseJsonArray(jsonNode.path("seoKeywords")));

            return result;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse optimization response: " + e.getMessage());
        }
    }

    private SkillSuggestionResult parseSkillSuggestionResponse(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            SkillSuggestionResult result = new SkillSuggestionResult();

            result.setTechnicalSkills(parseJsonArray(jsonNode.path("technicalSkills")));
            result.setSoftSkills(parseJsonArray(jsonNode.path("softSkills")));
            result.setTools(parseJsonArray(jsonNode.path("tools")));
            result.setCertifications(parseJsonArray(jsonNode.path("certifications")));

            return result;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse skill suggestion response: " + e.getMessage());
        }
    }

    private SalarySuggestionResult parseSalarySuggestionResponse(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            SalarySuggestionResult result = new SalarySuggestionResult();

            result.setCurrency(jsonNode.path("currency").asText());
            result.setMinSalary(jsonNode.path("minSalary").asDouble());
            result.setMaxSalary(jsonNode.path("maxSalary").asDouble());
            result.setAverageSalary(jsonNode.path("averageSalary").asDouble());
            result.setFactors(parseJsonArray(jsonNode.path("factors")));

            return result;
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse salary suggestion response: " + e.getMessage());
        }
    }

    private List<String> parseJsonArray(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                result.add(item.asText());
            }
        }
        return result;
    }

    // Result classes
    public static class JobDescriptionGenerationResult {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private String location;
        private String companyOverview;
        private String jobSummary;
        private List<String> responsibilities;
        private List<String> requiredQualifications;
        private List<String> preferredQualifications;
        private List<String> benefits;
        private String salaryRange;
        private long processingTime;

        // Getters and setters
        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public void setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getCompanyOverview() {
            return companyOverview;
        }

        public void setCompanyOverview(String companyOverview) {
            this.companyOverview = companyOverview;
        }

        public String getJobSummary() {
            return jobSummary;
        }

        public void setJobSummary(String jobSummary) {
            this.jobSummary = jobSummary;
        }

        public List<String> getResponsibilities() {
            return responsibilities;
        }

        public void setResponsibilities(List<String> responsibilities) {
            this.responsibilities = responsibilities;
        }

        public List<String> getRequiredQualifications() {
            return requiredQualifications;
        }

        public void setRequiredQualifications(List<String> requiredQualifications) {
            this.requiredQualifications = requiredQualifications;
        }

        public List<String> getPreferredQualifications() {
            return preferredQualifications;
        }

        public void setPreferredQualifications(List<String> preferredQualifications) {
            this.preferredQualifications = preferredQualifications;
        }

        public List<String> getBenefits() {
            return benefits;
        }

        public void setBenefits(List<String> benefits) {
            this.benefits = benefits;
        }

        public String getSalaryRange() {
            return salaryRange;
        }

        public void setSalaryRange(String salaryRange) {
            this.salaryRange = salaryRange;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }
    }

    public static class JobDescriptionOptimizationResult {
        private String optimizedTitle;
        private String optimizedDescription;
        private List<String> improvements;
        private List<String> seoKeywords;
        private int attractivenessScore;
        private long processingTime;

        // Getters and setters
        public String getOptimizedTitle() {
            return optimizedTitle;
        }

        public void setOptimizedTitle(String optimizedTitle) {
            this.optimizedTitle = optimizedTitle;
        }

        public String getOptimizedDescription() {
            return optimizedDescription;
        }

        public void setOptimizedDescription(String optimizedDescription) {
            this.optimizedDescription = optimizedDescription;
        }

        public List<String> getImprovements() {
            return improvements;
        }

        public void setImprovements(List<String> improvements) {
            this.improvements = improvements;
        }

        public List<String> getSeoKeywords() {
            return seoKeywords;
        }

        public void setSeoKeywords(List<String> seoKeywords) {
            this.seoKeywords = seoKeywords;
        }

        public int getAttractivenessScore() {
            return attractivenessScore;
        }

        public void setAttractivenessScore(int attractivenessScore) {
            this.attractivenessScore = attractivenessScore;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }
    }

    public static class SkillSuggestionResult {
        private List<String> technicalSkills;
        private List<String> softSkills;
        private List<String> tools;
        private List<String> certifications;
        private long processingTime;

        // Getters and setters
        public List<String> getTechnicalSkills() {
            return technicalSkills;
        }

        public void setTechnicalSkills(List<String> technicalSkills) {
            this.technicalSkills = technicalSkills;
        }

        public List<String> getSoftSkills() {
            return softSkills;
        }

        public void setSoftSkills(List<String> softSkills) {
            this.softSkills = softSkills;
        }

        public List<String> getTools() {
            return tools;
        }

        public void setTools(List<String> tools) {
            this.tools = tools;
        }

        public List<String> getCertifications() {
            return certifications;
        }

        public void setCertifications(List<String> certifications) {
            this.certifications = certifications;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }
    }

    public static class SalarySuggestionResult {
        private String currency;
        private double minSalary;
        private double maxSalary;
        private double averageSalary;
        private List<String> factors;
        private long processingTime;

        // Getters and setters
        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public double getMinSalary() {
            return minSalary;
        }

        public void setMinSalary(double minSalary) {
            this.minSalary = minSalary;
        }

        public double getMaxSalary() {
            return maxSalary;
        }

        public void setMaxSalary(double maxSalary) {
            this.maxSalary = maxSalary;
        }

        public double getAverageSalary() {
            return averageSalary;
        }

        public void setAverageSalary(double averageSalary) {
            this.averageSalary = averageSalary;
        }

        public List<String> getFactors() {
            return factors;
        }

        public void setFactors(List<String> factors) {
            this.factors = factors;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(long processingTime) {
            this.processingTime = processingTime;
        }
    }
}