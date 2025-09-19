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
     * @param jobTitle The job title
     * @param industry The industry sector
     * @param experienceLevel Experience level (junior, mid, senior, lead)
     * @param location Job location
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
            context.put("job_title", existingJob.getPostProfile());
            context.put("job_description", existingJob.getPostDesc());
            context.put("required_experience", existingJob.getReqExperience());
            context.put("tech_stack", existingJob.getPostTechStack());
            context.put("location", existingJob.getLocation());
            context.put("employment_type", existingJob.getEmployer());
            context.put("salary_min", existingJob.getSalaryMin());
            context.put("salary_max", existingJob.getSalaryMax());
            
            // Generate optimization suggestions
            String prompt = buildJobOptimizationPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Optimize job description");
            
            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to optimize job description: " + aiResponse.getMessage());
            }
            
            // Parse the AI response to extract structured data
            JobDescriptionOptimizationResult result = parseJobOptimizationResponse(aiResponse.getContent());
            result.setOriginalJobId(existingJob.getPostId());
            
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
     * Suggest relevant skills and technologies for a job
     * @param jobTitle The job title
     * @param industry The industry sector
     * @param experienceLevel Experience level
     * @return Suggested skills and technologies
     */
    public SkillSuggestionResult suggestSkills(String jobTitle, String industry, String experienceLevel) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                throw new IllegalArgumentException("Job title cannot be empty");
            }
            
            // Create context for skill suggestion
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", jobTitle);
            context.put("industry", industry != null ? industry : "technology");
            context.put("experience_level", experienceLevel != null ? experienceLevel : "mid");
            
            // Generate skill suggestions
            String prompt = buildSkillSuggestionPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Suggest relevant skills");
            
            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to suggest skills: " + aiResponse.getMessage());
            }
            
            // Parse the AI response to extract structured data
            SkillSuggestionResult result = parseSkillSuggestionResponse(aiResponse.getContent());
            result.setJobTitle(jobTitle);
            result.setIndustry(industry);
            result.setExperienceLevel(experienceLevel);
            
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
     * Suggest competitive salary ranges for a job
     * @param jobTitle The job title
     * @param industry The industry sector
     * @param experienceLevel Experience level
     * @param location Job location
     * @return Suggested salary ranges
     */
    public SalarySuggestionResult suggestSalaryRanges(
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
            
            // Create context for salary suggestion
            Map<String, Object> context = new HashMap<>();
            context.put("job_title", jobTitle);
            context.put("industry", industry != null ? industry : "technology");
            context.put("experience_level", experienceLevel != null ? experienceLevel : "mid");
            context.put("location", location != null ? location : "Dhaka, Bangladesh");
            
            // Generate salary suggestions
            String prompt = buildSalarySuggestionPrompt(context);
            AiResponseDto aiResponse = mistralAiService.generateText(prompt, "Suggest salary ranges");
            
            if (!aiResponse.isSuccess()) {
                throw new AiServiceException("Failed to suggest salary ranges: " + aiResponse.getMessage());
            }
            
            // Parse the AI response to extract structured data
            SalarySuggestionResult result = parseSalarySuggestionResponse(aiResponse.getContent());
            result.setJobTitle(jobTitle);
            result.setIndustry(industry);
            result.setExperienceLevel(experienceLevel);
            result.setLocation(location);
            
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
    
    /**
     * Build prompt for job description generation
     * @param context Context data
     * @return Formatted prompt
     */
    private String buildJobDescriptionPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert HR professional and job description writer with 15+ years of experience in creating compelling job postings that attract top talent. ");
        prompt.append("Your task is to create an optimized job description based on the provided information. ");
        prompt.append("Structure your response with clear sections and professional formatting.\n\n");
        
        prompt.append("JOB DETAILS:\n");
        prompt.append("- Job Title: ").append(context.get("job_title")).append("\n");
        prompt.append("- Industry: ").append(context.get("industry")).append("\n");
        prompt.append("- Experience Level: ").append(context.get("experience_level")).append("\n");
        prompt.append("- Location: ").append(context.get("location")).append("\n\n");
        
        prompt.append("Please create a comprehensive job description that includes the following sections:\n\n");
        
        prompt.append("1. JOB TITLE\n");
        prompt.append("   Provide an optimized job title that is attractive to candidates and searchable.\n\n");
        
        prompt.append("2. COMPANY OVERVIEW\n");
        prompt.append("   Write a brief company overview that highlights the organization's mission, values, and culture.\n\n");
        
        prompt.append("3. JOB SUMMARY\n");
        prompt.append("   Create a compelling job summary that clearly states the purpose of the role and its importance to the organization.\n\n");
        
        prompt.append("4. KEY RESPONSIBILITIES\n");
        prompt.append("   List 5-8 key responsibilities for this role, using bullet points with strong action verbs.\n");
        prompt.append("   Focus on outcomes and measurable results rather than just tasks.\n\n");
        
        prompt.append("5. REQUIRED QUALIFICATIONS\n");
        prompt.append("   Specify required qualifications including education, experience, and skills.\n");
        prompt.append("   Distinguish between 'must-have' and 'nice-to-have' qualifications.\n\n");
        
        prompt.append("6. PREFERRED QUALIFICATIONS\n");
        prompt.append("   List preferred qualifications that would make a candidate stand out.\n\n");
        
        prompt.append("7. TECHNICAL SKILLS\n");
        prompt.append("   Identify required and preferred technical skills relevant to this role.\n\n");
        
        prompt.append("8. SOFT SKILLS\n");
        prompt.append("   Specify important interpersonal and behavioral skills for success in this role.\n\n");
        
        prompt.append("9. BENEFITS AND PERKS\n");
        prompt.append("   Describe compensation, benefits, and perks offered to employees.\n\n");
        
        prompt.append("10. WORK ENVIRONMENT\n");
        prompt.append("    Describe the work environment, team structure, and reporting relationships.\n\n");
        
        prompt.append("FORMAT YOUR RESPONSE AS FOLLOWS:\n");
        prompt.append("---BEGIN STRUCTURED RESPONSE---\n");
        prompt.append("{\n");
        prompt.append("  \"jobTitle\": \"Optimized job title\",\n");
        prompt.append("  \"companyOverview\": \"Brief company overview\",\n");
        prompt.append("  \"jobSummary\": \"Compelling job summary\",\n");
        prompt.append("  \"responsibilities\": [\n");
        prompt.append("    \"Responsibility 1\",\n");
        prompt.append("    \"Responsibility 2\",\n");
        prompt.append("    \"Responsibility 3\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"requiredQualifications\": [\n");
        prompt.append("    \"Required qualification 1\",\n");
        prompt.append("    \"Required qualification 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"preferredQualifications\": [\n");
        prompt.append("    \"Preferred qualification 1\",\n");
        prompt.append("    \"Preferred qualification 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"technicalSkills\": [\n");
        prompt.append("    \"Technical skill 1\",\n");
        prompt.append("    \"Technical skill 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"softSkills\": [\n");
        prompt.append("    \"Soft skill 1\",\n");
        prompt.append("    \"Soft skill 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"benefits\": [\n");
        prompt.append("    \"Benefit 1\",\n");
        prompt.append("    \"Benefit 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"workEnvironment\": \"Description of work environment\"\n");
        prompt.append("}\n");
        prompt.append("---END STRUCTURED RESPONSE---\n\n");
        
        prompt.append("Ensure your response is formatted as valid JSON within the structured response markers.");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for job description optimization
     * @param context Context data
     * @return Formatted prompt
     */
    private String buildJobOptimizationPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert HR professional and job description optimizer with 15+ years of experience in improving job postings to attract better candidates and reduce time-to-hire. ");
        prompt.append("Your task is to analyze the provided job description and provide optimization suggestions. ");
        prompt.append("Structure your response with clear sections and professional formatting.\n\n");
        
        prompt.append("EXISTING JOB DESCRIPTION:\n");
        prompt.append("Job Title: ").append(context.get("job_title")).append("\n");
        prompt.append("Job Description: ").append(context.get("job_description")).append("\n");
        prompt.append("Required Experience: ").append(context.get("required_experience")).append("\n");
        prompt.append("Tech Stack: ").append(context.get("tech_stack")).append("\n");
        prompt.append("Location: ").append(context.get("location")).append("\n");
        prompt.append("Employment Type: ").append(context.get("employment_type")).append("\n");
        prompt.append("Salary Range: ").append(context.get("salary_min")).append(" - ").append(context.get("salary_max")).append("\n\n");
        
        prompt.append("Please analyze this job description and provide the following optimizations:\n\n");
        
        prompt.append("1. TITLE OPTIMIZATION\n");
        prompt.append("   Suggest an improved job title that is more attractive to candidates and SEO-friendly.\n\n");
        
        prompt.append("2. DESCRIPTION ENHANCEMENTS\n");
        prompt.append("   Identify areas where the job description can be improved for clarity, completeness, and吸引力.\n\n");
        
        prompt.append("3. RESPONSIBILITY CLARITY\n");
        prompt.append("   Suggest ways to make responsibilities more clear and outcome-focused.\n\n");
        
        prompt.append("4. QUALIFICATION BALANCING\n");
        prompt.append("   Review qualifications to ensure they are realistic and not overly restrictive.\n\n");
        
        prompt.append("5. SKILL ALIGNMENT\n");
        prompt.append("   Suggest additions or modifications to technical and soft skills sections.\n\n");
        
        prompt.append("6. BENEFIT HIGHLIGHTING\n");
        prompt.append("   Recommend ways to better showcase benefits and perks to attract candidates.\n\n");
        
        prompt.append("7. DIVERSITY AND INCLUSION\n");
        prompt.append("   Suggest language that promotes diversity and inclusion in hiring.\n\n");
        
        prompt.append("8. CALL TO ACTION\n");
        prompt.append("   Recommend improvements to the application process and next steps.\n\n");
        
        prompt.append("FORMAT YOUR RESPONSE AS FOLLOWS:\n");
        prompt.append("---BEGIN STRUCTURED RESPONSE---\n");
        prompt.append("{\n");
        prompt.append("  \"titleOptimization\": \"Improved job title\",\n");
        prompt.append("  \"descriptionEnhancements\": [\n");
        prompt.append("    \"Enhancement 1\",\n");
        prompt.append("    \"Enhancement 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"responsibilityImprovements\": [\n");
        prompt.append("    \"Improvement 1\",\n");
        prompt.append("    \"Improvement 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"qualificationBalancing\": [\n");
        prompt.append("    \"Balancing suggestion 1\",\n");
        prompt.append("    \"Balancing suggestion 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"skillAlignment\": [\n");
        prompt.append("    \"Alignment suggestion 1\",\n");
        prompt.append("    \"Alignment suggestion 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"benefitHighlighting\": [\n");
        prompt.append("    \"Highlighting suggestion 1\",\n");
        prompt.append("    \"Highlighting suggestion 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"diversityInclusion\": [\n");
        prompt.append("    \"D&I suggestion 1\",\n");
        prompt.append("    \"D&I suggestion 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"callToAction\": \"Improved call to action\"\n");
        prompt.append("}\n");
        prompt.append("---END STRUCTURED RESPONSE---\n\n");
        
        prompt.append("Ensure your response is formatted as valid JSON within the structured response markers.");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for skill suggestion
     * @param context Context data
     * @return Formatted prompt
     */
    private String buildSkillSuggestionPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert talent acquisition specialist and skills analyst with deep knowledge of industry requirements and skill trends. ");
        prompt.append("Your task is to suggest relevant skills and technologies for the specified job role. ");
        prompt.append("Structure your response with clear categories and professional formatting.\n\n");
        
        prompt.append("JOB ROLE:\n");
        prompt.append("- Job Title: ").append(context.get("job_title")).append("\n");
        prompt.append("- Industry: ").append(context.get("industry")).append("\n");
        prompt.append("- Experience Level: ").append(context.get("experience_level")).append("\n\n");
        
        prompt.append("Please provide skill suggestions organized by category:\n\n");
        
        prompt.append("1. TECHNICAL SKILLS\n");
        prompt.append("   List 10-15 essential technical skills for this role, organized by subcategories.\n\n");
        
        prompt.append("2. FRAMEWORKS AND TOOLS\n");
        prompt.append("   Identify popular frameworks, tools, and platforms relevant to this role.\n\n");
        
        prompt.append("3. DOMAIN KNOWLEDGE\n");
        prompt.append("   Specify domain-specific knowledge that would benefit candidates.\n\n");
        
        prompt.append("4. CERTIFICATIONS\n");
        prompt.append("   Recommend relevant certifications that add value to candidates.\n\n");
        
        prompt.append("5. EMERGING TECHNOLOGIES\n");
        prompt.append("   Highlight emerging technologies that may become important for this role.\n\n");
        
        prompt.append("6. SOFT SKILLS\n");
        prompt.append("   List important interpersonal and behavioral skills for success in this role.\n\n");
        
        prompt.append("FORMAT YOUR RESPONSE AS FOLLOWS:\n");
        prompt.append("---BEGIN STRUCTURED RESPONSE---\n");
        prompt.append("{\n");
        prompt.append("  \"technicalSkills\": {\n");
        prompt.append("    \"programmingLanguages\": [\"Language 1\", \"Language 2\"],\n");
        prompt.append("    \"databases\": [\"Database 1\", \"Database 2\"],\n");
        prompt.append("    \"cloudPlatforms\": [\"Platform 1\", \"Platform 2\"],\n");
        prompt.append("    \"devOps\": [\"DevOps tool 1\", \"DevOps tool 2\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"frameworksAndTools\": [\n");
        prompt.append("    \"Framework 1\",\n");
        prompt.append("    \"Tool 1\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"domainKnowledge\": [\n");
        prompt.append("    \"Domain knowledge 1\",\n");
        prompt.append("    \"Domain knowledge 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"certifications\": [\n");
        prompt.append("    \"Certification 1\",\n");
        prompt.append("    \"Certification 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"emergingTechnologies\": [\n");
        prompt.append("    \"Emerging tech 1\",\n");
        prompt.append("    \"Emerging tech 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"softSkills\": [\n");
        prompt.append("    \"Soft skill 1\",\n");
        prompt.append("    \"Soft skill 2\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("---END STRUCTURED RESPONSE---\n\n");
        
        prompt.append("Ensure your response is formatted as valid JSON within the structured response markers.");
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for salary suggestion
     * @param context Context data
     * @return Formatted prompt
     */
    private String buildSalarySuggestionPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert compensation analyst with deep knowledge of salary trends in the Bangladesh job market and global technology sector. ");
        prompt.append("Your task is to suggest competitive salary ranges for the specified job role. ");
        prompt.append("Structure your response with clear data and professional formatting.\n\n");
        
        prompt.append("JOB ROLE:\n");
        prompt.append("- Job Title: ").append(context.get("job_title")).append("\n");
        prompt.append("- Industry: ").append(context.get("industry")).append("\n");
        prompt.append("- Experience Level: ").append(context.get("experience_level")).append("\n");
        prompt.append("- Location: ").append(context.get("location")).append("\n\n");
        
        prompt.append("Please provide salary range suggestions with the following information:\n\n");
        
        prompt.append("1. SALARY RANGES\n");
        prompt.append("   Provide minimum, midpoint, and maximum salary ranges for this role.\n\n");
        
        prompt.append("2. MARKET POSITIONING\n");
        prompt.append("   Explain how these ranges compare to market averages for similar roles.\n\n");
        
        prompt.append("3. GEOGRAPHIC VARIATIONS\n");
        prompt.append("   If applicable, note how salaries vary by location within Bangladesh.\n\n");
        
        prompt.append("4. BENEFITS PACKAGE\n");
        prompt.append("   Suggest a comprehensive benefits package typical for this role and industry.\n\n");
        
        prompt.append("5. PERFORMANCE INCENTIVES\n");
        prompt.append("   Recommend performance-based incentives and bonus structures.\n\n");
        
        prompt.append("FORMAT YOUR RESPONSE AS FOLLOWS:\n");
        prompt.append("---BEGIN STRUCTURED RESPONSE---\n");
        prompt.append("{\n");
        prompt.append("  \"salaryRanges\": {\n");
        prompt.append("    \"currency\": \"BDT\",\n");
        prompt.append("    \"minimum\": 50000,\n");
        prompt.append("    \"midpoint\": 75000,\n");
        prompt.append("    \"maximum\": 100000\n");
        prompt.append("  },\n");
        prompt.append("  \"marketPositioning\": \"Explanation of market positioning\",\n");
        prompt.append("  \"geographicVariations\": {\n");
        prompt.append("    \"dhaka\": \"Dhaka range\",\n");
        prompt.append("    \"chittagong\": \"Chittagong range\",\n");
        prompt.append("    \"sylhet\": \"Sylhet range\"\n");
        prompt.append("  },\n");
        prompt.append("  \"benefitsPackage\": [\n");
        prompt.append("    \"Benefit 1\",\n");
        prompt.append("    \"Benefit 2\"\n");
        prompt.append("  ],\n");
        prompt.append("  \"performanceIncentives\": [\n");
        prompt.append("    \"Incentive 1\",\n");
        prompt.append("    \"Incentive 2\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("---END STRUCTURED RESPONSE---\n\n");
        
        prompt.append("Ensure your response is formatted as valid JSON within the structured response markers.");
        
        return prompt.toString();
    }
    
    /**
     * Parse job description generation response
     * @param response AI response content
     * @return Parsed job description generation result
     */
    private JobDescriptionGenerationResult parseJobDescriptionResponse(String response) {
        JobDescriptionGenerationResult result = new JobDescriptionGenerationResult();
        
        try {
            // Extract JSON from response (between markers)
            String jsonResponse = extractJsonFromResponse(response);
            if (jsonResponse == null) {
                throw new AiServiceException("Could not extract JSON from AI response");
            }
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // Extract fields
            if (root.has("jobTitle")) {
                result.setJobTitle(root.get("jobTitle").asText());
            }
            
            if (root.has("companyOverview")) {
                result.setCompanyOverview(root.get("companyOverview").asText());
            }
            
            if (root.has("jobSummary")) {
                result.setJobSummary(root.get("jobSummary").asText());
            }
            
            if (root.has("responsibilities") && root.get("responsibilities").isArray()) {
                List<String> responsibilities = new ArrayList<>();
                for (JsonNode node : root.get("responsibilities")) {
                    responsibilities.add(node.asText());
                }
                result.setResponsibilities(responsibilities);
            }
            
            if (root.has("requiredQualifications") && root.get("requiredQualifications").isArray()) {
                List<String> qualifications = new ArrayList<>();
                for (JsonNode node : root.get("requiredQualifications")) {
                    qualifications.add(node.asText());
                }
                result.setRequiredQualifications(qualifications);
            }
            
            if (root.has("preferredQualifications") && root.get("preferredQualifications").isArray()) {
                List<String> qualifications = new ArrayList<>();
                for (JsonNode node : root.get("preferredQualifications")) {
                    qualifications.add(node.asText());
                }
                result.setPreferredQualifications(qualifications);
            }
            
            if (root.has("technicalSkills") && root.get("technicalSkills").isArray()) {
                List<String> skills = new ArrayList<>();
                for (JsonNode node : root.get("technicalSkills")) {
                    skills.add(node.asText());
                }
                result.setTechnicalSkills(skills);
            }
            
            if (root.has("softSkills") && root.get("softSkills").isArray()) {
                List<String> skills = new ArrayList<>();
                for (JsonNode node : root.get("softSkills")) {
                    skills.add(node.asText());
                }
                result.setSoftSkills(skills);
            }
            
            if (root.has("benefits") && root.get("benefits").isArray()) {
                List<String> benefits = new ArrayList<>();
                for (JsonNode node : root.get("benefits")) {
                    benefits.add(node.asText());
                }
                result.setBenefits(benefits);
            }
            
            if (root.has("workEnvironment")) {
                result.setWorkEnvironment(root.get("workEnvironment").asText());
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing job description response: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values if parsing fails
            result.setCompanyOverview("Leading technology company in Bangladesh");
            result.setJobSummary("Exciting opportunity for a talented professional");
            result.setResponsibilities(Arrays.asList(
                "Develop and maintain software applications",
                "Collaborate with cross-functional teams",
                "Participate in code reviews and testing"
            ));
            result.setRequiredQualifications(Arrays.asList(
                "Bachelor's degree in Computer Science or related field",
                "3+ years of relevant experience"
            ));
            result.setPreferredQualifications(Arrays.asList(
                "Master's degree preferred",
                "Experience with cloud technologies"
            ));
            result.setTechnicalSkills(Arrays.asList("Java", "Spring Boot", "React"));
            result.setSoftSkills(Arrays.asList("Communication", "Teamwork", "Problem-solving"));
            result.setBenefits(Arrays.asList("Health insurance", "Flexible hours", "Professional development"));
            result.setWorkEnvironment("Collaborative and innovative workplace");
        }
        
        return result;
    }
    
    /**
     * Parse job optimization response
     * @param response AI response content
     * @return Parsed job optimization result
     */
    private JobDescriptionOptimizationResult parseJobOptimizationResponse(String response) {
        JobDescriptionOptimizationResult result = new JobDescriptionOptimizationResult();
        
        try {
            // Extract JSON from response (between markers)
            String jsonResponse = extractJsonFromResponse(response);
            if (jsonResponse == null) {
                throw new AiServiceException("Could not extract JSON from AI response");
            }
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // Extract fields
            if (root.has("titleOptimization")) {
                result.setTitleOptimization(root.get("titleOptimization").asText());
            }
            
            if (root.has("descriptionEnhancements") && root.get("descriptionEnhancements").isArray()) {
                List<String> enhancements = new ArrayList<>();
                for (JsonNode node : root.get("descriptionEnhancements")) {
                    enhancements.add(node.asText());
                }
                result.setDescriptionEnhancements(enhancements);
            }
            
            if (root.has("responsibilityImprovements") && root.get("responsibilityImprovements").isArray()) {
                List<String> improvements = new ArrayList<>();
                for (JsonNode node : root.get("responsibilityImprovements")) {
                    improvements.add(node.asText());
                }
                result.setResponsibilityImprovements(improvements);
            }
            
            if (root.has("qualificationBalancing") && root.get("qualificationBalancing").isArray()) {
                List<String> balancing = new ArrayList<>();
                for (JsonNode node : root.get("qualificationBalancing")) {
                    balancing.add(node.asText());
                }
                result.setQualificationBalancing(balancing);
            }
            
            if (root.has("skillAlignment") && root.get("skillAlignment").isArray()) {
                List<String> alignment = new ArrayList<>();
                for (JsonNode node : root.get("skillAlignment")) {
                    alignment.add(node.asText());
                }
                result.setSkillAlignment(alignment);
            }
            
            if (root.has("benefitHighlighting") && root.get("benefitHighlighting").isArray()) {
                List<String> highlighting = new ArrayList<>();
                for (JsonNode node : root.get("benefitHighlighting")) {
                    highlighting.add(node.asText());
                }
                result.setBenefitHighlighting(highlighting);
            }
            
            if (root.has("diversityInclusion") && root.get("diversityInclusion").isArray()) {
                List<String> diversity = new ArrayList<>();
                for (JsonNode node : root.get("diversityInclusion")) {
                    diversity.add(node.asText());
                }
                result.setDiversityInclusion(diversity);
            }
            
            if (root.has("callToAction")) {
                result.setCallToAction(root.get("callToAction").asText());
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing job optimization response: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values if parsing fails
            result.setTitleOptimization("Senior Software Engineer - Backend");
            result.setDescriptionEnhancements(Arrays.asList(
                "Add more details about company culture",
                "Clarify growth opportunities"
            ));
            result.setResponsibilityImprovements(Arrays.asList(
                "Make responsibilities more outcome-focused",
                "Add metrics for success"
            ));
            result.setQualificationBalancing(Arrays.asList(
                "Consider removing overly restrictive requirements",
                "Add alternative qualifications"
            ));
            result.setSkillAlignment(Arrays.asList(
                "Include emerging technologies",
                "Emphasize soft skills"
            ));
            result.setBenefitHighlighting(Arrays.asList(
                "Better showcase remote work options",
                "Highlight learning and development opportunities"
            ));
            result.setDiversityInclusion(Arrays.asList(
                "Use inclusive language",
                "Emphasize equal opportunity commitment"
            ));
            result.setCallToAction("Apply now to join our innovative team");
        }
        
        return result;
    }
    
    /**
     * Parse skill suggestion response
     * @param response AI response content
     * @return Parsed skill suggestion result
     */
    private SkillSuggestionResult parseSkillSuggestionResponse(String response) {
        SkillSuggestionResult result = new SkillSuggestionResult();
        
        try {
            // Extract JSON from response (between markers)
            String jsonResponse = extractJsonFromResponse(response);
            if (jsonResponse == null) {
                throw new AiServiceException("Could not extract JSON from AI response");
            }
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // Extract technical skills
            if (root.has("technicalSkills") && root.get("technicalSkills").isObject()) {
                Map<String, List<String>> technicalSkills = new HashMap<>();
                JsonNode techSkillsNode = root.get("technicalSkills");
                
                Iterator<Map.Entry<String, JsonNode>> fields = techSkillsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String categoryName = field.getKey();
                    JsonNode categoryNode = field.getValue();
                    
                    if (categoryNode.isArray()) {
                        List<String> skills = new ArrayList<>();
                        for (JsonNode node : categoryNode) {
                            skills.add(node.asText());
                        }
                        technicalSkills.put(categoryName, skills);
                    }
                }
                result.setTechnicalSkills(technicalSkills);
            }
            
            // Extract frameworks and tools
            if (root.has("frameworksAndTools") && root.get("frameworksAndTools").isArray()) {
                List<String> frameworksAndTools = new ArrayList<>();
                for (JsonNode node : root.get("frameworksAndTools")) {
                    frameworksAndTools.add(node.asText());
                }
                result.setFrameworksAndTools(frameworksAndTools);
            }
            
            // Extract domain knowledge
            if (root.has("domainKnowledge") && root.get("domainKnowledge").isArray()) {
                List<String> domainKnowledge = new ArrayList<>();
                for (JsonNode node : root.get("domainKnowledge")) {
                    domainKnowledge.add(node.asText());
                }
                result.setDomainKnowledge(domainKnowledge);
            }
            
            // Extract certifications
            if (root.has("certifications") && root.get("certifications").isArray()) {
                List<String> certifications = new ArrayList<>();
                for (JsonNode node : root.get("certifications")) {
                    certifications.add(node.asText());
                }
                result.setCertifications(certifications);
            }
            
            // Extract emerging technologies
            if (root.has("emergingTechnologies") && root.get("emergingTechnologies").isArray()) {
                List<String> emergingTechnologies = new ArrayList<>();
                for (JsonNode node : root.get("emergingTechnologies")) {
                    emergingTechnologies.add(node.asText());
                }
                result.setEmergingTechnologies(emergingTechnologies);
            }
            
            // Extract soft skills
            if (root.has("softSkills") && root.get("softSkills").isArray()) {
                List<String> softSkills = new ArrayList<>();
                for (JsonNode node : root.get("softSkills")) {
                    softSkills.add(node.asText());
                }
                result.setSoftSkills(softSkills);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing skill suggestion response: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values if parsing fails
            Map<String, List<String>> technicalSkills = new HashMap<>();
            technicalSkills.put("programmingLanguages", Arrays.asList("Java", "Python", "JavaScript"));
            technicalSkills.put("databases", Arrays.asList("PostgreSQL", "MongoDB"));
            technicalSkills.put("cloudPlatforms", Arrays.asList("AWS", "Azure"));
            technicalSkills.put("devOps", Arrays.asList("Docker", "Kubernetes"));
            result.setTechnicalSkills(technicalSkills);
            result.setFrameworksAndTools(Arrays.asList("Spring Boot", "React", "Node.js"));
            result.setDomainKnowledge(Arrays.asList("Financial services", "E-commerce"));
            result.setCertifications(Arrays.asList("AWS Certified Developer", "Oracle Certified Professional"));
            result.setEmergingTechnologies(Arrays.asList("AI/ML", "Blockchain", "IoT"));
            result.setSoftSkills(Arrays.asList("Leadership", "Communication", "Critical Thinking"));
        }
        
        return result;
    }
    
    /**
     * Parse salary suggestion response
     * @param response AI response content
     * @return Parsed salary suggestion result
     */
    private SalarySuggestionResult parseSalarySuggestionResponse(String response) {
        SalarySuggestionResult result = new SalarySuggestionResult();
        
        try {
            // Extract JSON from response (between markers)
            String jsonResponse = extractJsonFromResponse(response);
            if (jsonResponse == null) {
                throw new AiServiceException("Could not extract JSON from AI response");
            }
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // Extract salary ranges
            if (root.has("salaryRanges") && root.get("salaryRanges").isObject()) {
                JsonNode salaryRangesNode = root.get("salaryRanges");
                SalaryRange salaryRange = new SalaryRange();
                
                if (salaryRangesNode.has("currency")) {
                    salaryRange.setCurrency(salaryRangesNode.get("currency").asText());
                }
                
                if (salaryRangesNode.has("minimum")) {
                    salaryRange.setMinimum(salaryRangesNode.get("minimum").asInt());
                }
                
                if (salaryRangesNode.has("midpoint")) {
                    salaryRange.setMidpoint(salaryRangesNode.get("midpoint").asInt());
                }
                
                if (salaryRangesNode.has("maximum")) {
                    salaryRange.setMaximum(salaryRangesNode.get("maximum").asInt());
                }
                
                result.setSalaryRanges(salaryRange);
            }
            
            // Extract market positioning
            if (root.has("marketPositioning")) {
                result.setMarketPositioning(root.get("marketPositioning").asText());
            }
            
            // Extract geographic variations
            if (root.has("geographicVariations") && root.get("geographicVariations").isObject()) {
                Map<String, String> geographicVariations = new HashMap<>();
                JsonNode geoNode = root.get("geographicVariations");
                
                Iterator<Map.Entry<String, JsonNode>> fields = geoNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    geographicVariations.put(field.getKey(), field.getValue().asText());
                }
                result.setGeographicVariations(geographicVariations);
            }
            
            // Extract benefits package
            if (root.has("benefitsPackage") && root.get("benefitsPackage").isArray()) {
                List<String> benefitsPackage = new ArrayList<>();
                for (JsonNode node : root.get("benefitsPackage")) {
                    benefitsPackage.add(node.asText());
                }
                result.setBenefitsPackage(benefitsPackage);
            }
            
            // Extract performance incentives
            if (root.has("performanceIncentives") && root.get("performanceIncentives").isArray()) {
                List<String> performanceIncentives = new ArrayList<>();
                for (JsonNode node : root.get("performanceIncentives")) {
                    performanceIncentives.add(node.asText());
                }
                result.setPerformanceIncentives(performanceIncentives);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing salary suggestion response: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values if parsing fails
            SalaryRange salaryRange = new SalaryRange();
            salaryRange.setCurrency("BDT");
            salaryRange.setMinimum(60000);
            salaryRange.setMidpoint(80000);
            salaryRange.setMaximum(120000);
            result.setSalaryRanges(salaryRange);
            result.setMarketPositioning("Competitive with market averages");
            
            Map<String, String> geographicVariations = new HashMap<>();
            geographicVariations.put("dhaka", "60,000 - 120,000 BDT");
            geographicVariations.put("chittagong", "50,000 - 100,000 BDT");
            geographicVariations.put("sylhet", "55,000 - 110,000 BDT");
            result.setGeographicVariations(geographicVariations);
            
            result.setBenefitsPackage(Arrays.asList(
                "Health insurance", 
                "Provident fund", 
                "Annual bonuses",
                "Professional development allowance"
            ));
            result.setPerformanceIncentives(Arrays.asList(
                "Quarterly performance bonuses",
                "Stock options for senior positions",
                "Project completion bonuses"
            ));
        }
        
        return result;
    }
    
    /**
     * Extract JSON from AI response (between structured response markers)
     * @param response AI response content
     * @return Extracted JSON string or null if not found
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        // Look for JSON between markers
        String startMarker = "---BEGIN STRUCTURED RESPONSE---";
        String endMarker = "---END STRUCTURED RESPONSE---";
        
        int startIndex = response.indexOf(startMarker);
        if (startIndex == -1) {
            // If markers not found, try to parse the entire response as JSON
            return response.trim();
        }
        
        startIndex += startMarker.length();
        int endIndex = response.indexOf(endMarker, startIndex);
        
        if (endIndex == -1) {
            return null;
        }
        
        return response.substring(startIndex, endIndex).trim();
    }
    
    // DTOs for results
    
    public static class JobDescriptionGenerationResult {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private String location;
        private String companyOverview;
        private String jobSummary;
        private List<String> responsibilities = new ArrayList<>();
        private List<String> requiredQualifications = new ArrayList<>();
        private List<String> preferredQualifications = new ArrayList<>();
        private List<String> technicalSkills = new ArrayList<>();
        private List<String> softSkills = new ArrayList<>();
        private List<String> benefits = new ArrayList<>();
        private String workEnvironment;
        private long processingTime;
        
        // Getters and setters
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        
        public String getExperienceLevel() { return experienceLevel; }
        public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getCompanyOverview() { return companyOverview; }
        public void setCompanyOverview(String companyOverview) { this.companyOverview = companyOverview; }
        
        public String getJobSummary() { return jobSummary; }
        public void setJobSummary(String jobSummary) { this.jobSummary = jobSummary; }
        
        public List<String> getResponsibilities() { return responsibilities; }
        public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }
        
        public List<String> getRequiredQualifications() { return requiredQualifications; }
        public void setRequiredQualifications(List<String> requiredQualifications) { this.requiredQualifications = requiredQualifications; }
        
        public List<String> getPreferredQualifications() { return preferredQualifications; }
        public void setPreferredQualifications(List<String> preferredQualifications) { this.preferredQualifications = preferredQualifications; }
        
        public List<String> getTechnicalSkills() { return technicalSkills; }
        public void setTechnicalSkills(List<String> technicalSkills) { this.technicalSkills = technicalSkills; }
        
        public List<String> getSoftSkills() { return softSkills; }
        public void setSoftSkills(List<String> softSkills) { this.softSkills = softSkills; }
        
        public List<String> getBenefits() { return benefits; }
        public void setBenefits(List<String> benefits) { this.benefits = benefits; }
        
        public String getWorkEnvironment() { return workEnvironment; }
        public void setWorkEnvironment(String workEnvironment) { this.workEnvironment = workEnvironment; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
    }
    
    public static class JobDescriptionOptimizationResult {
        private Long originalJobId;
        private String titleOptimization;
        private List<String> descriptionEnhancements = new ArrayList<>();
        private List<String> responsibilityImprovements = new ArrayList<>();
        private List<String> qualificationBalancing = new ArrayList<>();
        private List<String> skillAlignment = new ArrayList<>();
        private List<String> benefitHighlighting = new ArrayList<>();
        private List<String> diversityInclusion = new ArrayList<>();
        private String callToAction;
        private long processingTime;
        
        // Getters and setters
        public Long getOriginalJobId() { return originalJobId; }
        public void setOriginalJobId(Long originalJobId) { this.originalJobId = originalJobId; }
        
        public String getTitleOptimization() { return titleOptimization; }
        public void setTitleOptimization(String titleOptimization) { this.titleOptimization = titleOptimization; }
        
        public List<String> getDescriptionEnhancements() { return descriptionEnhancements; }
        public void setDescriptionEnhancements(List<String> descriptionEnhancements) { this.descriptionEnhancements = descriptionEnhancements; }
        
        public List<String> getResponsibilityImprovements() { return responsibilityImprovements; }
        public void setResponsibilityImprovements(List<String> responsibilityImprovements) { this.responsibilityImprovements = responsibilityImprovements; }
        
        public List<String> getQualificationBalancing() { return qualificationBalancing; }
        public void setQualificationBalancing(List<String> qualificationBalancing) { this.qualificationBalancing = qualificationBalancing; }
        
        public List<String> getSkillAlignment() { return skillAlignment; }
        public void setSkillAlignment(List<String> skillAlignment) { this.skillAlignment = skillAlignment; }
        
        public List<String> getBenefitHighlighting() { return benefitHighlighting; }
        public void setBenefitHighlighting(List<String> benefitHighlighting) { this.benefitHighlighting = benefitHighlighting; }
        
        public List<String> getDiversityInclusion() { return diversityInclusion; }
        public void setDiversityInclusion(List<String> diversityInclusion) { this.diversityInclusion = diversityInclusion; }
        
        public String getCallToAction() { return callToAction; }
        public void setCallToAction(String callToAction) { this.callToAction = callToAction; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
    }
    
    public static class SkillSuggestionResult {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private Map<String, List<String>> technicalSkills = new HashMap<>();
        private List<String> frameworksAndTools = new ArrayList<>();
        private List<String> domainKnowledge = new ArrayList<>();
        private List<String> certifications = new ArrayList<>();
        private List<String> emergingTechnologies = new ArrayList<>();
        private List<String> softSkills = new ArrayList<>();
        private long processingTime;
        
        // Getters and setters
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        
        public String getExperienceLevel() { return experienceLevel; }
        public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
        
        public Map<String, List<String>> getTechnicalSkills() { return technicalSkills; }
        public void setTechnicalSkills(Map<String, List<String>> technicalSkills) { this.technicalSkills = technicalSkills; }
        
        public List<String> getFrameworksAndTools() { return frameworksAndTools; }
        public void setFrameworksAndTools(List<String> frameworksAndTools) { this.frameworksAndTools = frameworksAndTools; }
        
        public List<String> getDomainKnowledge() { return domainKnowledge; }
        public void setDomainKnowledge(List<String> domainKnowledge) { this.domainKnowledge = domainKnowledge; }
        
        public List<String> getCertifications() { return certifications; }
        public void setCertifications(List<String> certifications) { this.certifications = certifications; }
        
        public List<String> getEmergingTechnologies() { return emergingTechnologies; }
        public void setEmergingTechnologies(List<String> emergingTechnologies) { this.emergingTechnologies = emergingTechnologies; }
        
        public List<String> getSoftSkills() { return softSkills; }
        public void setSoftSkills(List<String> softSkills) { this.softSkills = softSkills; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
    }
    
    public static class SalaryRange {
        private String currency;
        private int minimum;
        private int midpoint;
        private int maximum;
        
        // Getters and setters
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public int getMinimum() { return minimum; }
        public void setMinimum(int minimum) { this.minimum = minimum; }
        
        public int getMidpoint() { return midpoint; }
        public void setMidpoint(int midpoint) { this.midpoint = midpoint; }
        
        public int getMaximum() { return maximum; }
        public void setMaximum(int maximum) { this.maximum = maximum; }
    }
    
    public static class SalarySuggestionResult {
        private String jobTitle;
        private String industry;
        private String experienceLevel;
        private String location;
        private SalaryRange salaryRanges;
        private String marketPositioning;
        private Map<String, String> geographicVariations = new HashMap<>();
        private List<String> benefitsPackage = new ArrayList<>();
        private List<String> performanceIncentives = new ArrayList<>();
        private long processingTime;
        
        // Getters and setters
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        
        public String getExperienceLevel() { return experienceLevel; }
        public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public SalaryRange getSalaryRanges() { return salaryRanges; }
        public void setSalaryRanges(SalaryRange salaryRanges) { this.salaryRanges = salaryRanges; }
        
        public String getMarketPositioning() { return marketPositioning; }
        public void setMarketPositioning(String marketPositioning) { this.marketPositioning = marketPositioning; }
        
        public Map<String, String> getGeographicVariations() { return geographicVariations; }
        public void setGeographicVariations(Map<String, String> geographicVariations) { this.geographicVariations = geographicVariations; }
        
        public List<String> getBenefitsPackage() { return benefitsPackage; }
        public void setBenefitsPackage(List<String> benefitsPackage) { this.benefitsPackage = benefitsPackage; }
        
        public List<String> getPerformanceIncentives() { return performanceIncentives; }
        public void setPerformanceIncentives(List<String> performanceIncentives) { this.performanceIncentives = performanceIncentives; }
        
        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
    }
}