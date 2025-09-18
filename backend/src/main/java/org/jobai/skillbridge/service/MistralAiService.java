package org.jobai.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.exception.AiServiceException;
import org.jobai.skillbridge.service.McpContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class MistralAiService {
    
    @Value("${mistral.api.token}")
    private String apiToken;
    
    @Value("${mistral.model.name:mistral-tiny}")
    private String modelName;
    
    @Autowired
    private McpContextService mcpContextService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";
    
    /**
     * Generate a resume for a user
     * @param userId The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @param format Optional resume format (chronological, functional, hybrid)
     * @param template Optional resume template (professional, creative, executive, etc.)
     * @return Generated resume content
     */
    public AiResponseDto generateResume(Long userId, String jobTitle, String format, String template) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (userId == null) {
                throw new AiServiceException("User ID cannot be null");
            }
            
            // Get structured context using MCP
            Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
            
            // Validate context
            if (context == null || context.isEmpty()) {
                throw new AiServiceException("Unable to generate user profile context");
            }
            
            // Create prompt for resume generation
            String prompt = buildResumePrompt(context, jobTitle, format, template);
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Failed to generate prompt for resume");
            }
            
            // Call Mistral API
            String resumeContent = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (resumeContent != null && resumeContent.startsWith("Error:")) {
                return new AiResponseDto(
                    resumeContent,
                    format,
                    false,
                    "Failed to generate resume: " + resumeContent,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                resumeContent,
                format,
                true,
                "Resume generated successfully",
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating resume: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                format,
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
    // (Remove the duplicate block entirely)
     * Generate a resume for a user (backward compatibility)
     * @param userId The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @param format Optional resume format (chronological, functional, hybrid)
     * @return Generated resume content
     */
    public AiResponseDto generateResume(Long userId, String jobTitle, String format) {
        return generateResume(userId, jobTitle, format, "professional");
    }
    
    /**
     * Generate a resume for a user (backward compatibility)
     * @param userId The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @return Generated resume content
     */
    public String generateResume(Long userId, String jobTitle) {
        AiResponseDto response = generateResume(userId, jobTitle, "chronological", "professional");
        return response.isSuccess() ? response.getContent() : response.getMessage();
    }
    
        /**
     * Optimize resume for a specific job
     * @param userId The user ID
     * @param jobId The job ID
     * @return Optimized resume content
     */
    public AiResponseDto optimizeResumeForJob(Long userId, Integer jobId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (userId == null) {
                throw new AiServiceException("User ID cannot be null");
            }
            
            if (jobId == null) {
                throw new AiServiceException("Job ID cannot be null");
            }
            
            // Get job-specific context using MCP
            Map<String, Object> context = mcpContextService.generateJobOptimizationContext(userId, jobId);
            
            // Validate context
            if (context == null || context.isEmpty()) {
                throw new AiServiceException("Unable to generate job optimization context");
            }
            
            // Create prompt for job-specific optimization
            String prompt = buildJobOptimizationPrompt(context, jobId);
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Failed to generate prompt for job optimization");
            }
            
            // Call Mistral API
            String optimizedResume = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (optimizedResume != null && optimizedResume.startsWith("Error:")) {
                return new AiResponseDto(
                    optimizedResume,
                    "job-optimized",
                    false,
                    "Failed to optimize resume: " + optimizedResume,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                optimizedResume,
                "job-optimized",
                true,
                "Resume optimized successfully for job ID: " + jobId,
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error optimizing resume: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                "job-optimized",
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
     * Build prompt for resume generation
     * @param context MCP context data
     * @param jobTitle Optional job title
     * @param format Optional resume format (chronological, functional, hybrid)
     * @param template Optional resume template (professional, creative, executive, etc.)
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle, String format, String template) {
        StringBuilder prompt = new StringBuilder();
        
        // Determine resume format and template
        String resumeFormat = format != null ? format.toLowerCase() : "chronological";
        String resumeTemplate = template != null ? template.toLowerCase() : "professional";
        
        // Professional resume writing guidelines based on template
        prompt.append("You are an expert resume writer with 20+ years of experience in crafting professional resumes for job seekers. ");
        
        // Template-specific instructions
        switch (resumeTemplate) {
            case "executive":
                prompt.append("Create an executive-level resume that emphasizes leadership, strategic thinking, and business results. ");
                prompt.append("Focus on high-level achievements, budget management, team leadership, and strategic initiatives. ");
                break;
            case "creative":
                prompt.append("Create a creative industry resume that showcases innovation, design thinking, and artistic accomplishments. ");
                prompt.append("Emphasize portfolio pieces, creative projects, and unique problem-solving approaches. ");
                break;
            case "academic":
                prompt.append("Create an academic resume that highlights research, publications, teaching experience, and scholarly achievements. ");
                prompt.append("Focus on educational background, research projects, publications, and academic contributions. ");
                break;
            case "technical":
                prompt.append("Create a technical resume that emphasizes technical skills, certifications, and hands-on experience. ");
                prompt.append("Focus on specific technologies, programming languages, systems, and technical projects. ");
                break;
            case "professional":
            default:
                prompt.append("Create a high-impact, results-driven resume that follows modern resume writing best practices. ");
                prompt.append("Focus on quantifiable achievements, action verbs, and clear, concise language. ");
                break;
        }
        
        prompt.append("Use professional terminology appropriate for the field. ");
        prompt.append("Structure the resume with clear section headings and consistent formatting. ");
        prompt.append("Ensure the resume is ATS (Applicant Tracking System) friendly.");
        
        // Format-specific instructions
        switch (resumeFormat) {
            case "functional":
                prompt.append("Generate a professional functional resume format that emphasizes skills and achievements over chronological work history. ");
                prompt.append("Organize content by skill categories with specific examples of achievements. ");
                prompt.append("Include a brief work history section at the end. ");
                break;
            case "hybrid":
                prompt.append("Generate a professional hybrid resume format that combines elements of chronological and functional formats. ");
                prompt.append("Highlight both skills/achievements and work history in a balanced manner. ");
                break;
            case "chronological":
            default:
                prompt.append("Generate a professional chronological resume format that lists work experience in reverse chronological order. ");
                prompt.append("Emphasize career progression and quantifiable achievements. ");
                break;
        }
        
        prompt.append("for a ");
        if (jobTitle != null && !jobTitle.isEmpty()) {
            prompt.append(jobTitle).append(" based on the following profile. ");
            prompt.append("Tailor the resume to highlight skills and experiences most relevant to this role. ");
            prompt.append("Incorporate industry-specific keywords and terminology for ").append(jobTitle).append(".");
        } else {
            prompt.append("professional based on the following profile");
        }
        
        // Format context data into readable prompt
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
            prompt.append("Name: ").append(personalInfo.get("name")).append("");
            prompt.append("Email: ").append(personalInfo.get("email")).append("");
            if (personalInfo.get("phone") != null) {
                prompt.append("Phone: ").append(personalInfo.get("phone")).append("");
            }
            if (personalInfo.get("address") != null) {
                prompt.append("Address: ").append(personalInfo.get("address")).append("");
            }
            prompt.append("Location: ").append(personalInfo.get("city")).append(", ")
                  .append(personalInfo.get("country")).append("");
        }
        
        // Add bio/professional summary if available, or create one
        if (personalInfo != null && personalInfo.get("bio") != null && !personalInfo.get("bio").toString().trim().isEmpty()) {
            prompt.append("Professional Summary").append(personalInfo.get("bio")).append("");
        } else {
            prompt.append("Professional Summary");
            prompt.append("Write a compelling 2-3 sentence professional summary that highlights the candidate's key strengths, ");
            prompt.append("years of experience, and career goals. Tailor it to the ").append(jobTitle != null ? jobTitle : "professional").append(" role");
        }
        
        // Add skills section with more details and professional formatting
        prompt.append("Skills:");

        prompt.append("Organize skills into relevant categories. For each skill, include proficiency level if provided.");

        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            // Group skills by category if available
            Map<String, List<Map<String, Object>>> skillsByCategory = new HashMap<>();
            for (Map<String, Object> skill : skills) {
                String category = (String) skill.get("category");
                if (category == null || category.trim().isEmpty()) category = "General";
                skillsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(skill);
            }
            
            for (Map.Entry<String, List<Map<String, Object>>> entry : skillsByCategory.entrySet()) {
                prompt.append(entry.getKey()).append("");
                List<String> skillNames = new ArrayList<>();
                for (Map<String, Object> skill : entry.getValue()) {
                    StringBuilder skillEntry = new StringBuilder("- ").append(skill.get("name"));
                    Object proficiency = skill.get("proficiency_level");
                    if (proficiency != null) {
                        skillEntry.append(" (Level: ").append(proficiency).append("/10)");
                    }
                    skillNames.add(skillEntry.toString());
                }
                prompt.append(String.join("", skillNames)).append("");
            }
        } else {
            prompt.append("No specific skills provided. Include relevant skills for the ").append(jobTitle != null ? jobTitle : "professional").append(" role.");
        }
        
        // Add education with more details and professional formatting
        prompt.append("Education");
        prompt.append("List educational qualifications in reverse chronological order. Include institution, degree, field of study, and dates.");
        List<Map<String, Object>> educations = (List<Map<String, Object>>) context.get("education");
        if (educations != null && !educations.isEmpty()) {
            for (Map<String, Object> edu : educations) {
                prompt.append("- ").append(edu.get("degree")).append(" in ")
                      .append(edu.get("field_of_study")).append("");
                prompt.append("  ").append(edu.get("institution")).append("");
                Object startDate = edu.get("start_date");
                Object endDate = edu.get("end_date");
                if (startDate != null || endDate != null) {
                    prompt.append("  ");
                    if (startDate != null) prompt.append(formatDate(startDate.toString()));
                    if (endDate != null) {
                        prompt.append(" - ").append(formatDate(endDate.toString()));
                    } else {
                        prompt.append(" - Present");
                    }
                    prompt.append("");
                }
                Object grade = edu.get("grade");
                if (grade != null && !grade.toString().trim().isEmpty()) {
                    prompt.append("  Grade: ").append(grade).append("");
                }
                Object description = edu.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    prompt.append("  ").append(description).append("");
                }
                prompt.append("");
            }
        } else {
            prompt.append("No formal education history provided. Include relevant educational qualifications.");
        }
        
        // Add experience with more details and achievement-focused formatting
        prompt.append("Work Experience:");
        prompt.append("List work experience in reverse chronological order. For each position, include company name, job title, and dates. ");
        prompt.append("Use bullet points to highlight key achievements with quantifiable results where possible. ");
        prompt.append("Start each bullet point with a strong action verb.");
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null && !experiences.isEmpty()) {
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append("");
                prompt.append("  ").append(exp.get("company")).append("");
                Object startDate = exp.get("start_date");
                Object endDate = exp.get("end_date");
                Object currentlyWorking = exp.get("currently_working");
                if (startDate != null || endDate != null) {
                    prompt.append("  ");
                    if (startDate != null) prompt.append(formatDate(startDate.toString()));
                    if (Boolean.TRUE.equals(currentlyWorking)) {
                        prompt.append(" - Present");
                    } else if (endDate != null) {
                        prompt.append(" - ").append(formatDate(endDate.toString()));
                    }
                    prompt.append("");
                }
                Object description = exp.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    // Try to parse description as achievements if it contains bullet points or numbered lists
                    String descStr = description.toString().trim();
                    if (descStr.contains("") || descStr.contains(";") || descStr.contains("-")) {
                        prompt.append("  Key Achievements:");
                        // Format as bullet points if not already formatted
                        if (!descStr.contains("-") && !descStr.contains("*")) {
                            String[] achievements = descStr.contains(";") ? descStr.split(";") : descStr.split("");
                            for (String achievement : achievements) {
                                if (!achievement.trim().isEmpty()) {
                                    prompt.append("  - ").append(achievement.trim()).append("");
                                }
                            }
                        } else {
                            prompt.append("  ").append(descStr).append("");
                        }
                    } else {
                        prompt.append("  Key Achievements:");
                        prompt.append("  - ").append(descStr).append("");
                    }
                } else {
                    prompt.append("  Key Achievements");
                    prompt.append("  - [Include 3-5 quantifiable achievements with specific results]");
                }
                prompt.append("");
            }
        } else {
            prompt.append("No formal work experience provided. Include relevant professional experience or projects.");
        }
        
        // Add portfolio if available
        List<Map<String, Object>> portfolios = (List<Map<String, Object>>) context.get("portfolio");
        if (portfolios != null && !portfolios.isEmpty()) {
            prompt.append("Portfolio:");
            prompt.append("List relevant projects, publications, or work samples that demonstrate expertise");
            for (Map<String, Object> portfolio : portfolios) {
                prompt.append("- ").append(portfolio.get("title")).append("");
                Object description = portfolio.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    prompt.append("  ").append(description).append("");
                }
                Object url = portfolio.get("url");
                if (url != null && !url.toString().trim().isEmpty()) {
                    prompt.append("  URL: ").append(url).append("");
                }
                prompt.append("");
            }
        }
        
        prompt.append("\n\nPlease structure this as a professional resume with the following guidelines:\n");
        String resumeFormatValue = resumeFormat != null ? resumeFormat.toLowerCase() : "chronological";
        switch (resumeFormatValue) {
            case "functional":
                prompt.append("- Start with a professional summary\n");
                prompt.append("- Follow with a detailed skills section organized by category\n");
                prompt.append("- Include key achievements grouped by skill area\n");
                prompt.append("- End with a brief chronological work history\n");
                prompt.append("- Keep to 1-2 pages maximum\n");
                break;
            case "hybrid":
                prompt.append("- Start with a professional summary\n");
                prompt.append("- Follow with a skills section highlighting core competencies\n");
                prompt.append("- Detail work experience in reverse chronological order with achievements\n");
                prompt.append("- Include education and additional relevant sections\n");
                prompt.append("- Keep to 1-2 pages maximum\n");
                break;
            case "chronological":
            default:
                prompt.append("- Start with a professional summary\n");
                prompt.append("- Detail work experience first in reverse chronological order\n");
                prompt.append("- Highlight achievements with quantifiable results\n");
                prompt.append("- Follow with education and skills sections\n");
                prompt.append("- Keep to 1-2 pages maximum\n");
                break;
        }

        // Template-specific formatting instructions
        switch (resumeTemplate != null ? resumeTemplate.toLowerCase() : "professional") {
            case "executive":
                prompt.append("- Emphasize leadership roles and team management\n");
                prompt.append("- Highlight budget responsibilities and financial results\n");
                prompt.append("- Include strategic initiatives and business impact\n");
                prompt.append("- Use executive-level language and terminology\n");
                break;
            case "creative":
                prompt.append("- Emphasize portfolio pieces and creative projects\n");
                prompt.append("- Include visual or design-related achievements\n");
                prompt.append("- Highlight innovative problem-solving approaches\n");
                prompt.append("- Use creative industry terminology\n");
                break;
            case "academic":
                prompt.append("- Emphasize research projects and publications\n");
                prompt.append("- Include teaching experience and academic contributions\n");
                prompt.append("- Highlight educational achievements and honors\n");
                prompt.append("- Use academic terminology and formatting\n");
                break;
            case "technical":
                prompt.append("- Emphasize technical skills and certifications\n");
                prompt.append("- Include specific technologies and programming languages\n");
                prompt.append("- Highlight technical projects and system implementations\n");
                prompt.append("- Use technical terminology and acronyms\n");
                break;
            case "professional":
            default:
                prompt.append("- Use standard professional formatting\n");
                prompt.append("- Emphasize achievements with quantifiable results\n");
                prompt.append("- Include relevant industry terminology\n");
                break;
        }
        
prompt.append("- Use clear section headings in ALL CAPS or bold formatting\n");
prompt.append("- Use bullet points for achievements and responsibilities\n");
prompt.append("- Start each bullet point with a strong action verb (Managed, Developed, Implemented, etc.)\n");
prompt.append("- Include quantifiable results where possible (Increased sales by 25%, Managed a team of 10, etc.)\n");
prompt.append("- Use consistent date formatting (Month YYYY - Month YYYY)\n");
prompt.append("- Maintain a professional tone throughout\n");
prompt.append("- Format the resume in clean, readable text without any markdown or special formatting\n");
prompt.append("- Ensure the resume is ATS-friendly with standard section headings\n");
prompt.append("- Tailor the content to the ").append(jobTitle != null ? jobTitle : "professional").append(" role\n");
        
        prompt.append("Final Output: Provide only the resume content without any additional explanations or comments.");
        
        return prompt.toString();
    }
    

    /**
     * Build prompt for resume generation (backward compatibility)
     * @param context MCP context data
     * @param jobTitle Optional job title
     * @param format Optional resume format (chronological, functional, hybrid)
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle, String format) {
        return buildResumePrompt(context, jobTitle, format, "professional");
    }
    
    /**
     * Format date string for resume display
     * @param dateStr Date string to format
     * @return Formatted date string
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        
        // Handle different date formats
        try {
            // If it's already in a readable format, return as is
            if (dateStr.contains(" ") || dateStr.contains(",")) {
                return dateStr;
            }
            
            // Handle ISO date format (YYYY-MM-DD)
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                     "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    int monthIndex = Integer.parseInt(parts[1]) - 1;
                    if (monthIndex >= 0 && monthIndex < 12) {
                        return months[monthIndex] + " " + parts[0];
                    }
                }
            }
            
            // Return original if we can't format it
            return dateStr;
        } catch (Exception e) {
            // Return original if there's any error
            return dateStr;
        }
    }
    
    /**
     * Build prompt for resume generation (backward compatibility)
     * @param context MCP context data
     * @param jobTitle Optional job title
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle) {
        return buildResumePrompt(context, jobTitle, "chronological");
    }
    
    /**
     * Build prompt for job optimization
     * @param context MCP context data
     * @param jobId Job ID
     * @return Formatted prompt
     */
    private String buildJobOptimizationPrompt(Map<String, Object> context, Integer jobId) {
        StringBuilder prompt = new StringBuilder();
        
        // Professional resume writing guidelines
        prompt.append("You are an expert resume writer with 20+ years of experience in crafting professional resumes for job seekers. ");
        prompt.append("Your task is to optimize the following resume specifically for the job described below. ");
        prompt.append("Focus on highlighting the most relevant skills, experiences, and achievements that match the job requirements. ");
        prompt.append("Use industry-specific keywords and terminology that would resonate with the hiring manager for this role. ");
        prompt.append("Structure the resume with clear section headings and consistent formatting. ");
        prompt.append("Ensure the resume is ATS (Applicant Tracking System) friendly.\n\n");
        
        // Job information
        Map<String, Object> jobInfo = (Map<String, Object>) context.get("job_info");
        if (jobInfo != null) {
            prompt.append("JOB DETAILS:\n");
            prompt.append("Position: ").append(jobInfo.get("title")).append("\n");
            prompt.append("Description: ").append(jobInfo.get("description")).append("\n");
            if (jobInfo.get("required_experience") != null) {
                prompt.append("Required Experience: ").append(jobInfo.get("required_experience")).append(" years\n");
            }
            if (jobInfo.get("tech_stack") != null) {
                prompt.append("Required Skills/Technologies: ").append(jobInfo.get("tech_stack")).append("\n");
            }
            if (jobInfo.get("location") != null) {
                prompt.append("Location: ").append(jobInfo.get("location")).append("\n");
            }
            if (jobInfo.get("employment_type") != null) {
                prompt.append("Employment Type: ").append(jobInfo.get("employment_type")).append("\n");
            }
            prompt.append("\n");
        }
        
        // Instructions for optimization
        prompt.append("OPTIMIZATION INSTRUCTIONS:\n");
        prompt.append("1. Customize the professional summary to align with the job requirements\n");
        prompt.append("2. Reorder and rephrase work experiences to emphasize relevant achievements\n");
        prompt.append("3. Highlight skills that match the job description keywords\n");
        prompt.append("4. Quantify achievements with specific metrics where possible\n");
        prompt.append("5. Use action verbs and industry-specific terminology\n");
        prompt.append("6. Ensure the resume is concise (1-2 pages) and well-structured\n\n");
        
        // Current resume information (from user profile)
        prompt.append("CURRENT RESUME:\n\n");
        
        // Personal info
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
            prompt.append("Name: ").append(personalInfo.get("name")).append("\n");
            prompt.append("Email: ").append(personalInfo.get("email")).append("\n");
            if (personalInfo.get("phone") != null) {
                prompt.append("Phone: ").append(personalInfo.get("phone")).append("\n");
            }
            if (personalInfo.get("address") != null) {
                prompt.append("Address: ").append(personalInfo.get("address")).append("\n");
            }
            prompt.append("Location: ").append(personalInfo.get("city")).append(", ")
                  .append(personalInfo.get("country")).append("\n");
            
            if (personalInfo.get("bio") != null && !personalInfo.get("bio").toString().trim().isEmpty()) {
                prompt.append("Professional Summary: ").append(personalInfo.get("bio")).append("\n");
            }
            prompt.append("\n");
        }
        
        // Skills
        prompt.append("Skills:\n");
        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            Map<String, List<Map<String, Object>>> skillsByCategory = new HashMap<>();
            for (Map<String, Object> skill : skills) {
                String category = (String) skill.get("category");
                if (category == null || category.trim().isEmpty()) category = "General";
                skillsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(skill);
            }
            
            for (Map.Entry<String, List<Map<String, Object>>> entry : skillsByCategory.entrySet()) {
                prompt.append(entry.getKey()).append(":\n");
                List<String> skillNames = new ArrayList<>();
                for (Map<String, Object> skill : entry.getValue()) {
                    StringBuilder skillEntry = new StringBuilder("- ").append(skill.get("name"));
                    Object proficiency = skill.get("proficiency_level");
                    if (proficiency != null) {
                        skillEntry.append(" (Level: ").append(proficiency).append("/10)");
                    }
                    skillNames.add(skillEntry.toString());
                }
                prompt.append(String.join("\n", skillNames)).append("\n");
            }
        } else {
            prompt.append("No specific skills provided.\n");
        }
        prompt.append("\n");
        
        // Education
        prompt.append("Education:\n");
        List<Map<String, Object>> educations = (List<Map<String, Object>>) context.get("education");
        if (educations != null && !educations.isEmpty()) {
            for (Map<String, Object> edu : educations) {
                prompt.append("- ").append(edu.get("degree")).append(" in ")
                      .append(edu.get("field_of_study")).append("\n");
                prompt.append("  ").append(edu.get("institution")).append("\n");
                Object startDate = edu.get("start_date");
                Object endDate = edu.get("end_date");
                if (startDate != null || endDate != null) {
                    prompt.append("  ");
                    if (startDate != null) prompt.append(formatDate(startDate.toString()));
                    if (endDate != null) {
                        prompt.append(" - ").append(formatDate(endDate.toString()));
                    } else {
                        prompt.append(" - Present");
                    }
                    prompt.append("\n");
                }
                Object grade = edu.get("grade");
                if (grade != null && !grade.toString().trim().isEmpty()) {
                    prompt.append("  Grade: ").append(grade).append("\n");
                }
                Object description = edu.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    prompt.append("  ").append(description).append("\n");
                }
                prompt.append("\n");
            }
        } else {
            prompt.append("No formal education history provided.\n\n");
        }
        
        // Experience
        prompt.append("Work Experience:\n");
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null && !experiences.isEmpty()) {
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append("\n");
                prompt.append("  ").append(exp.get("company")).append("\n");
                Object startDate = exp.get("start_date");
                Object endDate = exp.get("end_date");
                Object currentlyWorking = exp.get("currently_working");
                if (startDate != null || endDate != null) {
                    prompt.append("  ");
                    if (startDate != null) prompt.append(formatDate(startDate.toString()));
                    if (Boolean.TRUE.equals(currentlyWorking)) {
                        prompt.append(" - Present");
                    } else if (endDate != null) {
                        prompt.append(" - ").append(formatDate(endDate.toString()));
                    }
                    prompt.append("\n");
                }
                Object description = exp.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    String descStr = description.toString().trim();
                    if (descStr.contains("\n") || descStr.contains(";") || descStr.contains("-")) {
                        prompt.append("  Key Achievements:\n");
                        if (!descStr.contains("-") && !descStr.contains("*")) {
                            String[] achievements = descStr.contains(";") ? descStr.split(";") : descStr.split("\n");
                            for (String achievement : achievements) {
                                if (!achievement.trim().isEmpty()) {
                                    prompt.append("  - ").append(achievement.trim()).append("\n");
                                }
                            }
                        } else {
                            prompt.append("  ").append(descStr).append("\n");
                        }
                    } else {
                        prompt.append("  Key Achievements:\n");
                        prompt.append("  - ").append(descStr).append("\n");
                    }
                } else {
                    prompt.append("  Key Achievements:\n");
                    prompt.append("  - [Include 3-5 quantifiable achievements with specific results]\n");
                }
                prompt.append("\n");
            }
        } else {
            prompt.append("No formal work experience provided.\n\n");
        }
        
        // Portfolio
        List<Map<String, Object>> portfolios = (List<Map<String, Object>>) context.get("portfolio");
        if (portfolios != null && !portfolios.isEmpty()) {
            prompt.append("Portfolio:\n");
            prompt.append("List relevant projects, publications, or work samples that demonstrate expertise\n");
            for (Map<String, Object> portfolio : portfolios) {
                prompt.append("- ").append(portfolio.get("title")).append("\n");
                Object description = portfolio.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    prompt.append("  ").append(description).append("\n");
                }
                Object url = portfolio.get("url");
                if (url != null && !url.toString().trim().isEmpty()) {
                    prompt.append("  URL: ").append(url).append("\n");
                }
                prompt.append("\n");
            }
        }
        
        // Final instructions
        prompt.append("\nPlease optimize this resume specifically for the job ID: ").append(jobId).append(".\n");
        prompt.append("Focus on matching the job requirements with the candidate's most relevant experiences and skills.\n");
        prompt.append("Return only the optimized resume content without any additional explanations or comments.");
        
        return prompt.toString();
    }
    
    /**
     * Generate interview preparation questions for a user
     * @param userId The user ID
     * @param jobId Optional job ID to tailor questions for
     * @return Generated interview questions and preparation guide
     */
    public AiResponseDto generateInterviewPreparation(Long userId, Integer jobId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (userId == null) {
                throw new AiServiceException("User ID cannot be null");
            }
            
            // Get structured context using MCP
            Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
            
            // If job ID is provided, get job-specific context
            if (jobId != null) {
                Map<String, Object> jobContext = mcpContextService.generateJobOptimizationContext(userId, jobId);
                context.putAll(jobContext);
            }
            
            // Validate context
            if (context == null || context.isEmpty()) {
                throw new AiServiceException("Unable to generate user profile context");
            }
            
            // Create prompt for interview preparation
            String prompt = buildInterviewPreparationPrompt(context, jobId);
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Failed to generate prompt for interview preparation");
            }
            
            // Call Mistral API
            String preparationContent = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (preparationContent != null && preparationContent.startsWith("Error:")) {
                return new AiResponseDto(
                    preparationContent,
                    "interview-preparation",
                    false,
                    "Failed to generate interview preparation: " + preparationContent,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                preparationContent,
                "interview-preparation",
                true,
                "Interview preparation generated successfully",
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating interview preparation: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                "interview-preparation",
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
     * Build prompt for interview preparation
     * @param context MCP context data
     * @param jobId Optional job ID
     * @return Formatted prompt
     */
    private String buildInterviewPreparationPrompt(Map<String, Object> context, Integer jobId) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert career coach and interview preparation specialist with 15+ years of experience ");
        prompt.append("helping job seekers succeed in technical and behavioral interviews. ");
        prompt.append("Your task is to create a comprehensive interview preparation guide based on the candidate's profile ");
        if (jobId != null) {
            prompt.append("and tailored to the specific job they're applying for. ");
        } else {
            prompt.append("and general best practices. ");
        }
        prompt.append("Provide actionable advice, sample questions, and strategies for success.\n\n");
        
        // Job information (if available)
        Map<String, Object> jobInfo = (Map<String, Object>) context.get("job_info");
        if (jobInfo != null && jobId != null) {
            prompt.append("TARGET JOB:\n");
            prompt.append("Position: ").append(jobInfo.get("title")).append("\n");
            prompt.append("Description: ").append(jobInfo.get("description")).append("\n");
            if (jobInfo.get("required_experience") != null) {
                prompt.append("Required Experience: ").append(jobInfo.get("required_experience")).append(" years\n");
            }
            if (jobInfo.get("tech_stack") != null) {
                prompt.append("Required Skills/Technologies: ").append(jobInfo.get("tech_stack")).append("\n");
            }
            prompt.append("\n");
        }
        
        // Candidate information
        prompt.append("CANDIDATE PROFILE:\n\n");
        
        // Personal info
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
            prompt.append("Name: ").append(personalInfo.get("name")).append("\n");
            if (personalInfo.get("bio") != null && !personalInfo.get("bio").toString().trim().isEmpty()) {
                prompt.append("Professional Summary: ").append(personalInfo.get("bio")).append("\n");
            }
            prompt.append("\n");
        }
        
        // Skills
        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            prompt.append("Key Skills:\n");
            for (Map<String, Object> skill : skills) {
                prompt.append("- ").append(skill.get("name"));
                if (skill.get("category") != null) {
                    prompt.append(" (Category: ").append(skill.get("category")).append(")");
                }
                if (skill.get("proficiency_level") != null) {
                    prompt.append(" (Proficiency: ").append(skill.get("proficiency_level")).append("/10)");
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // Experience
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null && !experiences.isEmpty()) {
            prompt.append("Work Experience:\n");
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append(" at ").append(exp.get("company")).append("\n");
                if (exp.get("description") != null) {
                    prompt.append("  ").append(exp.get("description")).append("\n");
                }
                prompt.append("\n");
            }
        }
        
        // Education
        List<Map<String, Object>> educations = (List<Map<String, Object>>) context.get("education");
        if (educations != null && !educations.isEmpty()) {
            prompt.append("Education:\n");
            for (Map<String, Object> edu : educations) {
                prompt.append("- ").append(edu.get("degree")).append(" in ").append(edu.get("field_of_study"));
                if (edu.get("institution") != null) {
                    prompt.append(" from ").append(edu.get("institution"));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // Preparation structure
        prompt.append("Please structure your response as follows:\n\n");
        
        prompt.append("1. INTERVIEW OVERVIEW\n");
        prompt.append("   - Types of interviews to expect (technical, behavioral, HR, etc.)\n");
        if (jobId != null) {
            prompt.append("   - Role-specific interview expectations\n");
        }
        prompt.append("   - Typical interview process duration and stages\n\n");
        
        prompt.append("2. COMMON INTERVIEW QUESTIONS\n");
        prompt.append("   A. Behavioral Questions (use STAR method format)\n");
        prompt.append("      - Tell me about yourself\n");
        prompt.append("      - Why do you want to work here?\n");
        prompt.append("      - Describe a challenging project and how you handled it\n");
        prompt.append("      - What are your strengths and weaknesses?\n");
        prompt.append("      - Where do you see yourself in 5 years?\n\n");
        
        prompt.append("   B. Technical Questions\n");
        if (skills != null && !skills.isEmpty()) {
            prompt.append("      (Based on the candidate's skills)\n");
        } else {
            prompt.append("      (General technical questions for the role)\n");
        }
        prompt.append("      - Problem-solving scenarios\n");
        prompt.append("      - System design questions (if applicable)\n");
        prompt.append("      - Coding challenges (if applicable)\n\n");
        
        prompt.append("3. ANSWER STRATEGIES\n");
        prompt.append("   - How to structure responses using the STAR method\n");
        prompt.append("   - Techniques for handling difficult questions\n");
        prompt.append("   - Tips for showcasing achievements and skills\n");
        prompt.append("   - How to address employment gaps or career changes\n\n");
        
        prompt.append("4. ROLE-SPECIFIC PREPARATION\n");
        if (jobId != null && jobInfo != null) {
            prompt.append("   - Industry-specific knowledge for the ").append(jobInfo.get("title")).append(" role\n");
            if (jobInfo.get("tech_stack") != null) {
                prompt.append("   - Technical preparation for required technologies\n");
            }
        } else {
            prompt.append("   - General industry knowledge and trends\n");
        }
        prompt.append("   - Company research strategies\n");
        prompt.append("   - Understanding the company culture and values\n\n");
        
        prompt.append("5. PRACTICAL EXERCISES\n");
        prompt.append("   - Mock interview scenarios\n");
        prompt.append("   - Self-practice techniques\n");
        prompt.append("   - How to get feedback on responses\n\n");
        
        prompt.append("6. DO'S AND DON'TS\n");
        prompt.append("   - Professional presentation and attire\n");
        prompt.append("   - Communication tips (tone, pace, clarity)\n");
        prompt.append("   - Body language and non-verbal cues\n");
        prompt.append("   - What to avoid during interviews\n\n");
        
        prompt.append("7. FOLLOW-UP STRATEGIES\n");
        prompt.append("   - Sending thank-you notes\n");
        prompt.append("   - Handling rejection constructively\n");
        prompt.append("   - Negotiating offers\n\n");
        
        prompt.append("Provide specific, actionable advice tailored to this candidate's background and ");
        if (jobId != null) {
            prompt.append("the target job role. ");
        } else {
            prompt.append("general interview best practices. ");
        }
        prompt.append("Include examples and practical exercises where appropriate.");
        
        return prompt.toString();
    }
    
    /**
     * Call Mistral API
     * @param prompt The prompt to send
     * @return Generated text response
     */
    private String callMistralApi(String prompt) {
        System.out.println("Mistral API Token: " + (apiToken != null ? "[PROVIDED]" : "[NOT PROVIDED]"));
        try {
            // Validate API token
            if (apiToken == null || apiToken.trim().isEmpty()) {
                return "Error: Mistral API token is not configured. Please set MISTRAL_API_TOKEN in your .env file.";
            }
            
            // Validate model name
            if (modelName == null || modelName.trim().isEmpty()) {
                return "Error: Mistral model name is not configured.";
            }
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                return "Error: Prompt cannot be empty.";
            }
            
            RestTemplate restTemplate = new RestTemplate();
            
            // Construct URL
            String url = MISTRAL_API_URL;
            System.out.println("Calling Mistral API at URL: " + url);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiToken.trim());
            
            // Create request body for Mistral API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            
            // Create messages array
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> messageObj = new HashMap<>();
            messageObj.put("role", "user");
            messageObj.put("content", prompt);
            messages.add(messageObj);
            requestBody.put("messages", messages);
            
            // Add parameters
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            requestBody.put("top_p", 0.9);
            requestBody.put("stream", false);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            System.out.println("Sending request to Mistral API with prompt length: " + prompt.length());
            
            // Log request body (without sensitive data)
            Map<String, Object> loggedRequestBody = new HashMap<>(requestBody);
            System.out.println("Request body: " + new ObjectMapper().writeValueAsString(loggedRequestBody));
            
            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("Received response from Mistral API with status: " + response.getStatusCode());
            System.out.println("Response body length: " + (response.getBody() != null ? response.getBody().length() : 0));
            
            // Handle different response statuses
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "Error: Model '" + modelName + "' not found. Please check the model name in your configuration.";
            }
            
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "Error: Invalid Mistral API token. Please check your MISTRAL_API_TOKEN in .env file.";
            }
            
            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "Error: Access forbidden to Mistral model '" + modelName + "'. This might be due to model permissions or rate limiting.";
            }
            
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Error: Rate limit exceeded for Mistral API. Please try again later.";
            }
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error calling Mistral API. Status: " + response.getStatusCode() + ". Response: " + response.getBody();
            }
            
            // Parse response
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                // Handle Mistral API response format
                if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                    JsonNode firstChoice = root.get("choices").get(0);
                    if (firstChoice.has("message")) {
                        JsonNode message = firstChoice.get("message");
                        if (message.has("content")) {
                            return message.get("content").asText();
                        }
                    }
                }
                
                // Special handling for Mistral API error responses
                if (root.has("message")) {
                    String errorMessage = root.get("message").asText();
                    if (errorMessage.contains("not found") || errorMessage.contains("not exist")) {
                        return "Error: Model '" + modelName + "' not found. Please check the model name in your configuration. Mistral API message: " + errorMessage;
                    }
                    if (errorMessage.contains("Unauthorized") || errorMessage.contains("invalid")) {
                        return "Error: Invalid Mistral API token. Please check your MISTRAL_API_TOKEN in .env file. Mistral API message: " + errorMessage;
                    }
                    return "Error from Mistral API: " + errorMessage;
                }
                
                // If we can't parse as JSON, return raw response
                return response.getBody();
                
            } catch (Exception jsonParseError) {
                // If JSON parsing fails, return the raw response
                System.out.println("JSON parsing failed, returning raw response: " + response.getBody());
                return response.getBody();
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("404 Not Found error when calling Mistral API");
            System.err.println("Model: " + modelName);
            System.err.println("URL: " + MISTRAL_API_URL);
            e.printStackTrace();
            return "Error: Model '" + modelName + "' not found (404). Please check the model name in your configuration.";
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("401 Unauthorized error when calling Mistral API");
            System.err.println("Token: " + (apiToken != null ? "Provided" : "Not provided"));
            e.printStackTrace();
            return "Error: Invalid Mistral API token (401). Please check your MISTRAL_API_TOKEN in .env file.";
        } catch (HttpClientErrorException.Forbidden e) {
            System.err.println("403 Forbidden error when calling Mistral API");
            e.printStackTrace();
            return "Error: Access forbidden to Mistral model '" + modelName + "' (403). This might be due to model permissions or rate limiting.";
        } catch (HttpClientErrorException.TooManyRequests e) {
            System.err.println("429 Too Many Requests error when calling Mistral API");
            e.printStackTrace();
            return "Error: Rate limit exceeded for Mistral API. Please try again later.";
        } catch (Exception e) {
            System.err.println("General error when calling Mistral API");
            e.printStackTrace();
            return "Error generating content: " + e.getMessage();
        }
    }
    
    /**
     * Generate fallback resume when AI service is not available
     * @param prompt The original prompt
     * @return A basic resume structure
     */
    private String generateFallbackResume(String prompt) {
        // Extract job title from prompt if available
        String jobTitle = "Professional";
        if (prompt != null && prompt.contains("Generate a professional resume for a")) {
            int startIndex = prompt.indexOf("Generate a professional resume for a") + 35;
            int endIndex = prompt.indexOf(" based on the following profile");
            if (startIndex > 34 && endIndex > startIndex) {
                jobTitle = prompt.substring(startIndex, endIndex).trim();
            }
        }
        
return "==================== RESUME ====================\n"
    + "NAME: [User Name]\n"
    + "EMAIL: [User Email]\n"
    + "PHONE: [User Phone]\n"
    + "LOCATION: [User City, Country]\n\n"
    + "PROFESSIONAL SUMMARY:\n"
    + "Highly motivated and results-driven " + jobTitle + " with [X] years of experience in [industry/field]. "
    + "Proven track record of [key achievement or skill]. Seeking to leverage expertise in [relevant area] "
    + "to contribute to organizational success.\n\n"
    + "SKILLS:\n"
    + "TECHNICAL SKILLS:\n"
    + "- [Skill 1] (Proficiency: /10)\n"
    + "- [Skill 2] (Proficiency: /10)\n"
    + "- [Skill 3] (Proficiency: /10)\n\n"
    + "SOFT SKILLS:\n"
    + "- [Soft Skill 1]\n"
    + "- [Soft Skill 2]\n"
    + "- [Soft Skill 3]\n\n"
    + "PROFESSIONAL EXPERIENCE:\n"
    + "[Job Title]\n"
    + "[Company Name], [Location]\n"
    + "[Start Date] - [End Date]\n"
    + "Key Achievements:\n"
    + "- [Quantifiable achievement with metrics]\n"
    + "- [Project or initiative you led]\n"
    + "- [Result or improvement you delivered]\n\n"
    + "EDUCATION:\n"
    + "[Degree] in [Field of Study]\n"
    + "[University Name], [Location]\n"
    + "[Graduation Date]\n"
    + "[Relevant coursework or honors]\n\n"
    + "================================================\n\n"
    + "Note: This is a fallback resume template. To generate an AI-powered resume:\n"
    + "1. Ensure you have configured your Mistral API token in the .env file\n"
    + "2. Set MISTRAL_API_TOKEN=your_actual_token_here\n"
    + "3. Optionally set MISTRAL_MODEL_NAME to your preferred model\n"
    + "4. Restart the application\n\n"
    + "For more information, visit: https://mistral.ai/";
    }
    
    /**
     * Generate interview questions for a user based on job and skills
     * @param userId The user ID
     * @param jobId Optional job ID to tailor questions for
     * @return Generated interview questions
     */
    public AiResponseDto generateInterviewQuestions(Long userId, Integer jobId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (userId == null) {
                throw new AiServiceException("User ID cannot be null");
            }
            
            // Get structured context using MCP
            Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
            
            // If job ID is provided, get job-specific context
            if (jobId != null) {
                context = mcpContextService.generateJobOptimizationContext(userId, jobId);
            }
            
            // Validate context
            if (context == null || context.isEmpty()) {
                throw new AiServiceException("Unable to generate user profile context");
            }
            
            // Create prompt for interview questions
            String prompt = buildInterviewQuestionsPrompt(context, jobId);
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Failed to generate prompt for interview questions");
            }
            
            // Call Mistral API
            String questionsContent = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (questionsContent != null && questionsContent.startsWith("Error:")) {
                return new AiResponseDto(
                    questionsContent,
                    "interview-questions",
                    false,
                    "Failed to generate interview questions: " + questionsContent,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                questionsContent,
                "interview-questions",
                true,
                "Interview questions generated successfully",
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating interview questions: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                "interview-questions",
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
     * Build prompt for interview questions
     * @param context MCP context data
     * @param jobId Optional job ID
     * @return Formatted prompt
     */
    private String buildInterviewQuestionsPrompt(Map<String, Object> context, Integer jobId) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert HR professional and interview coach with extensive experience in preparing candidates for job interviews. ");
        prompt.append("Your task is to generate comprehensive interview questions and preparation guidance for the candidate based on their profile ");
        
        if (jobId != null) {
            prompt.append("and the specific job they are applying for (Job ID: ").append(jobId).append("). ");
        } else {
            prompt.append("and general professional best practices. ");
        }
        
        prompt.append("Structure your response as follows:\n");

        
prompt.append("1. COMMON INTERVIEW QUESTIONS\n");
prompt.append("   - Provide 10-15 common interview questions with suggested approaches\n");
prompt.append("   - Include both behavioral and technical questions where relevant\n");
prompt.append("   - Offer tips on how to structure responses using the STAR method (Situation, Task, Action, Result)\n\n");
        
        prompt.append("2. ROLE-SPECIFIC QUESTIONS");
        if (jobId != null) {
            prompt.append("   - Generate 5-10 questions specifically tailored to the job requirements\n");
            prompt.append("   - Focus on skills and experiences mentioned in the job description\n");
            prompt.append("   - Include questions about challenges specific to the role");
        } else {
            prompt.append("   - Generate 5-10 questions based on the candidate's skills and experience");
            prompt.append("   - Focus on areas where the candidate has significant experienc");
        }
        prompt.append("\n\n");
        
        prompt.append("3. CANDIDATE TAILORED QUESTIONS");
        prompt.append("   - Create 5 questions that directly relate to the candidate's background\n");
        prompt.append("   - Reference specific experiences, skills, or achievements from their profile  \n");
        prompt.append("   - Include questions that allow the candidate to showcase their unique strengths\n\n");
        
        prompt.append("4. ANSWER GUIDELINE");
        prompt.append("   - For each question, provide a framework for crafting strong responses");
        prompt.append("   - Include specific examples of what good answers might include");
        prompt.append("   - Highlight common pitfalls to avoid");
        
        prompt.append("5. INDUSTRY INSIGHTS");
        prompt.append("   - Share current trends and expectations in the candidate's field");
        prompt.append("   - Mention any specific qualities or skills that are particularly valued");
        prompt.append("   - Provide context about the company culture if job details are available");
        
        prompt.append("Use the following candidate profile information for your question generation:");
        
        // Add personal info
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
prompt.append("Candidate Information:\n");
            prompt.append("- Name: ").append(personalInfo.get("name")).append("");
            if (personalInfo.get("bio") != null) {
                prompt.append("- Professional Summary: ").append(personalInfo.get("bio")).append("");
            }
            prompt.append("");
        }
        
        // Add skills
        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            prompt.append("Key Skills:");
            for (Map<String, Object> skill : skills) {
                prompt.append("- ").append(skill.get("name"));
                if (skill.get("category") != null) {
                    prompt.append(" (Category: ").append(skill.get("category")).append(")");
                }
                if (skill.get("proficiency_level") != null) {
                    prompt.append(" (Self-rated: ").append(skill.get("proficiency_level")).append("/10)");
                }
                prompt.append("");
            }
            prompt.append("");
        }
        
        // Add experience
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null && !experiences.isEmpty()) {
            prompt.append("Work Experience");
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append(" at ").append(exp.get("company"));
                if (exp.get("description") != null) {
                    prompt.append(": ").append(exp.get("description"));
                }
                prompt.append("");
            }
            prompt.append("");
        }
        
        // Add job info if available
        Map<String, Object> jobInfo = (Map<String, Object>) context.get("job_info");
        if (jobInfo != null) {
            prompt.append("Target Job Information:");
            prompt.append("- Position: ").append(jobInfo.get("title")).append("");
            prompt.append("- Description: ").append(jobInfo.get("description")).append("");
            if (jobInfo.get("tech_stack") != null) {
                prompt.append("- Required Skills/Technologies: ").append(jobInfo.get("tech_stack")).append("");
            }
            prompt.append("");
        }
        
        prompt.append("Please provide comprehensive interview preparation materials based on this information.");
        
        return prompt.toString();
    }

    
    /**
     * Generate skill assessment for a user
     * @param userId The user ID
     * @param skillName Optional specific skill to assess
     * @return Skill assessment content
     */
    public AiResponseDto generateSkillAssessment(Long userId, String skillName) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (userId == null) {
                throw new AiServiceException("User ID cannot be null");
            }
            
            // Get structured context using MCP
            Map<String, Object> context = mcpContextService.generateUserProfileContext(userId);
            
            // Validate context
            if (context == null || context.isEmpty()) {
                throw new AiServiceException("Unable to generate user profile context");
            }
            
            // Create prompt for skill assessment
            String prompt = buildSkillAssessmentPrompt(context, skillName);
            
            // Validate prompt
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Failed to generate prompt for skill assessment");
            }
            
            // Call Mistral API
            String assessmentContent = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (assessmentContent != null && assessmentContent.startsWith("Error:")) {
                return new AiResponseDto(
                    assessmentContent,
                    "skill-assessment",
                    false,
                    "Failed to generate skill assessment: " + assessmentContent,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                assessmentContent,
                "skill-assessment",
                true,
                "Skill assessment generated successfully",
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating skill assessment: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                "skill-assessment",
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
     * Generate generic text from a prompt
     * @param prompt The prompt to generate text from
     * @param responseType The type of response (for categorization)
     * @return Generated text response
     */
    public AiResponseDto generateText(String prompt, String responseType) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate inputs
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Prompt cannot be null or empty");
            }
            
            // Validate response type
            if (responseType == null || responseType.trim().isEmpty()) {
                responseType = "generic";
            }
            
            // Call Mistral API
            String generatedText = callMistralApi(prompt);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Check for error messages in the response
            if (generatedText != null && generatedText.startsWith("Error:")) {
                return new AiResponseDto(
                    generatedText,
                    responseType,
                    false,
                    "Failed to generate text: " + generatedText,
                    processingTime
                );
            }
            
            return new AiResponseDto(
                generatedText,
                responseType,
                true,
                "Text generated successfully",
                processingTime
            );
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating text: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            return new AiResponseDto(
                null,
                responseType,
                false,
                errorMessage,
                processingTime
            );
        }
    }
    
    /**
     * Build prompt for skill assessment
     * @param context MCP context data
     * @param skillName Optional specific skill to assess
     * @return Formatted prompt
     */
    private String buildSkillAssessmentPrompt(Map<String, Object> context, String skillName) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert skills assessor with extensive experience in evaluating professional competencies. ");
        prompt.append("Your task is to create a comprehensive skill assessment for the user based on their profile. ");
        
        if (skillName != null && !skillName.isEmpty()) {
            prompt.append("Focus specifically on assessing the user's proficiency in ").append(skillName).append(". ");
        } else {
            prompt.append("Provide a comprehensive assessment of all the user's skills. ");
        }
        
        prompt.append("Structure your response as follows:\n\n");
        prompt.append("1. SKILL ASSESSMENT SUMMARY\n");
        prompt.append("   - Overall skill level (Beginner, Intermediate, Advanced, Expert)\n");
        prompt.append("   - Key strengths\n");
        prompt.append("   - Areas for improvement\n\n");
        
        if (skillName != null && !skillName.isEmpty()) {
            prompt.append("2. DETAILED ").append(skillName.toUpperCase()).append(" ASSESSMENT\n");
            prompt.append("   - Current proficiency level (1-10 scale)\n");
            prompt.append("   - Years of experience\n");
            prompt.append("   - Notable achievements\n");
            prompt.append("   - Specific strengths\n");
            prompt.append("   - Improvement recommendations\n\n");
        } else {
            prompt.append("2. DETAILED SKILL ASSESSMENTS\n");
            prompt.append("   For each skill, provide:\n");
            prompt.append("   - Skill name\n");
            prompt.append("   - Proficiency level (1-10 scale)\n");
            prompt.append("   - Years of experience\n");
            prompt.append("   - Notable achievements\n");
            prompt.append("   - Specific strengths\n");
            prompt.append("   - Improvement recommendations\n\n");
        }
        
        prompt.append("3. LEARNING PATH RECOMMENDATIONS\n");
        prompt.append("   - Suggested courses or certifications\n");
        prompt.append("   - Recommended projects or practice exercises\n");
        prompt.append("   - Industry resources for skill development\n\n");
        
        prompt.append("4. MARKET INSIGHTS\n");
        prompt.append("   - Current market demand for these skills\n");
        prompt.append("   - Salary ranges associated with different proficiency levels\n");
        prompt.append("   - Emerging trends in the skill area\n\n");
        
        prompt.append("Use the following user profile information for your assessment:\n\n");
        
        // Add personal info
        Map<String, Object> personalInfo = (Map<String, Object>) context.get("personal_info");
        if (personalInfo != null) {
            prompt.append("Personal Information:\n");
            prompt.append("- Name: ").append(personalInfo.get("name")).append("\n");
            if (personalInfo.get("bio") != null) {
                prompt.append("- Professional Summary: ").append(personalInfo.get("bio")).append("\n");
            }
            prompt.append("\n");
        }
        
        // Add skills
        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            prompt.append("Current Skills:\n");
            for (Map<String, Object> skill : skills) {
                prompt.append("- ").append(skill.get("name"));
                if (skill.get("category") != null) {
                    prompt.append(" (Category: ").append(skill.get("category")).append(")");
                }
                if (skill.get("proficiency_level") != null) {
                    prompt.append(" (Self-rated: ").append(skill.get("proficiency_level")).append("/10)");
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // Add experience
        List<Map<String, Object>> experiences = (List<Map<String, Object>>) context.get("experience");
        if (experiences != null && !experiences.isEmpty()) {
            prompt.append("Work Experience:\n");
            for (Map<String, Object> exp : experiences) {
                prompt.append("- ").append(exp.get("position")).append(" at ").append(exp.get("company"));
                if (exp.get("description") != null) {
                    prompt.append(": ").append(exp.get("description"));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        // Add education
        List<Map<String, Object>> educations = (List<Map<String, Object>>) context.get("education");
        if (educations != null && !educations.isEmpty()) {
            prompt.append("Education:\n");
            for (Map<String, Object> edu : educations) {
                prompt.append("- ").append(edu.get("degree")).append(" in ").append(edu.get("field_of_study"));
                if (edu.get("institution") != null) {
                    prompt.append(" from ").append(edu.get("institution"));
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("\nPlease provide your comprehensive skill assessment based on this information.");
        
        return prompt.toString();
    }
}