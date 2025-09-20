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
     * 
     * @param userId   The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @param format   Optional resume format (chronological, functional, hybrid)
     * @param template Optional resume template (professional, creative, executive,
     *                 etc.)
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
                        processingTime);
            }

            return new AiResponseDto(
                    resumeContent,
                    format,
                    true,
                    "Resume generated successfully",
                    processingTime);
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
                    processingTime);
        }
    }

    /**
     * Generate a resume for a user (backward compatibility)
     * 
     * @param userId   The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @param format   Optional resume format (chronological, functional, hybrid)
     * @return Generated resume content
     */
    public AiResponseDto generateResume(Long userId, String jobTitle, String format) {
        return generateResume(userId, jobTitle, format, "professional");
    }

    /**
     * Generate a resume for a user (backward compatibility)
     * 
     * @param userId   The user ID
     * @param jobTitle Optional job title to tailor the resume for
     * @return Generated resume content
     */
    public String generateResume(Long userId, String jobTitle) {
        AiResponseDto response = generateResume(userId, jobTitle, "chronological", "professional");
        return response.isSuccess() ? response.getContent() : response.getMessage();
    }

    /**
     * Optimize resume for a specific job
     * 
     * @param userId The user ID
     * @param jobId  The job ID
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
                        processingTime);
            }

            return new AiResponseDto(
                    optimizedResume,
                    "job-optimized",
                    true,
                    "Resume optimized successfully for job ID: " + jobId,
                    processingTime);
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
                    processingTime);
        }
    }

    /**
     * Generate text using Mistral AI
     * 
     * @param prompt  The prompt to send to AI
     * @param context Optional context description
     * @return AI response
     */
    public AiResponseDto generateText(String prompt, String context) {
        long startTime = System.currentTimeMillis();

        try {
            // Validate inputs
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new AiServiceException("Prompt cannot be empty");
            }

            // Call Mistral API
            String content = callMistralApi(prompt);

            long processingTime = System.currentTimeMillis() - startTime;

            // Check for error messages in the response
            if (content != null && content.startsWith("Error:")) {
                return new AiResponseDto(
                        content,
                        "text",
                        false,
                        "Failed to generate text: " + content,
                        processingTime);
            }

            return new AiResponseDto(
                    content,
                    "text",
                    true,
                    context != null ? context + " completed successfully" : "Text generation completed successfully",
                    processingTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            String errorMessage = "Error generating text: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();

            return new AiResponseDto(
                    null,
                    "text",
                    false,
                    errorMessage,
                    processingTime);
        }
    }

    /**
     * Build prompt for resume generation
     * 
     * @param context  MCP context data
     * @param jobTitle Optional job title
     * @param format   Optional resume format (chronological, functional, hybrid)
     * @param template Optional resume template (professional, creative, executive,
     *                 etc.)
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle, String format, String template) {
        StringBuilder prompt = new StringBuilder();

        // Determine resume format and template
        String resumeFormat = format != null ? format.toLowerCase() : "chronological";
        String resumeTemplate = template != null ? template.toLowerCase() : "professional";

        // Professional resume writing guidelines based on template
        prompt.append(
                "You are an expert resume writer with 20+ years of experience in crafting professional resumes for job seekers. ");

        // Template-specific instructions
        switch (resumeTemplate) {
            case "executive":
                prompt.append(
                        "Create an executive-level resume that emphasizes leadership, strategic thinking, and business results. ");
                prompt.append(
                        "Focus on high-level achievements, budget management, team leadership, and strategic initiatives. ");
                break;
            case "creative":
                prompt.append(
                        "Create a creative industry resume that showcases innovation, design thinking, and artistic accomplishments. ");
                prompt.append("Emphasize portfolio pieces, creative projects, and unique problem-solving approaches. ");
                break;
            case "academic":
                prompt.append(
                        "Create an academic resume that highlights research, publications, teaching experience, and scholarly achievements. ");
                prompt.append(
                        "Focus on educational background, research projects, publications, and academic contributions. ");
                break;
            case "technical":
                prompt.append(
                        "Create a technical resume that emphasizes technical skills, certifications, and hands-on experience. ");
                prompt.append(
                        "Focus on specific technologies, programming languages, systems, and technical projects. ");
                break;
            case "professional":
            default:
                prompt.append(
                        "Create a high-impact, results-driven resume that follows modern resume writing best practices. ");
                prompt.append("Focus on quantifiable achievements, action verbs, and clear, concise language. ");
                break;
        }

        prompt.append("Use professional terminology appropriate for the field. ");
        prompt.append("Structure the resume with clear section headings and consistent formatting. ");
        prompt.append("Ensure the resume is ATS (Applicant Tracking System) friendly.");

        // Format-specific instructions
        switch (resumeFormat) {
            case "functional":
                prompt.append(
                        "Generate a professional functional resume format that emphasizes skills and achievements over chronological work history. ");
                prompt.append("Organize content by skill categories with specific examples of achievements. ");
                prompt.append("Include a brief work history section at the end. ");
                break;
            case "hybrid":
                prompt.append(
                        "Generate a professional hybrid resume format that combines elements of chronological and functional formats. ");
                prompt.append("Highlight both skills/achievements and work history in a balanced manner. ");
                break;
            case "chronological":
            default:
                prompt.append(
                        "Generate a professional chronological resume format that lists work experience in reverse chronological order. ");
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
        if (personalInfo != null && personalInfo.get("bio") != null
                && !personalInfo.get("bio").toString().trim().isEmpty()) {
            prompt.append("Professional Summary").append(personalInfo.get("bio")).append("");
        } else {
            prompt.append("Professional Summary");
            prompt.append(
                    "Write a compelling 2-3 sentence professional summary that highlights the candidate's key strengths, ");
            prompt.append("years of experience, and career goals. Tailor it to the ")
                    .append(jobTitle != null ? jobTitle : "professional").append(" role");
        }

        // Add skills section with more details and professional formatting
        prompt.append("Skills:");

        prompt.append(
                "Organize skills into relevant categories. For each skill, include proficiency level if provided.");

        List<Map<String, Object>> skills = (List<Map<String, Object>>) context.get("skills");
        if (skills != null && !skills.isEmpty()) {
            // Group skills by category if available
            Map<String, List<Map<String, Object>>> skillsByCategory = new HashMap<>();
            for (Map<String, Object> skill : skills) {
                String category = (String) skill.get("category");
                if (category == null || category.trim().isEmpty())
                    category = "General";
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
            prompt.append("No specific skills provided. Include relevant skills for the ")
                    .append(jobTitle != null ? jobTitle : "professional").append(" role.");
        }

        // Add education with more details and professional formatting
        prompt.append("Education");
        prompt.append(
                "List educational qualifications in reverse chronological order. Include institution, degree, field of study, and dates.");
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
                    if (startDate != null)
                        prompt.append(formatDate(startDate.toString()));
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
        prompt.append(
                "List work experience in reverse chronological order. For each position, include company name, job title, and dates. ");
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
                    if (startDate != null)
                        prompt.append(formatDate(startDate.toString()));
                    if (Boolean.TRUE.equals(currentlyWorking)) {
                        prompt.append(" - Present");
                    } else if (endDate != null) {
                        prompt.append(" - ").append(formatDate(endDate.toString()));
                    }
                    prompt.append("");
                }
                Object description = exp.get("description");
                if (description != null && !description.toString().trim().isEmpty()) {
                    // Try to parse description as achievements if it contains bullet points or
                    // numbered lists
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
        prompt.append(
                "- Include quantifiable results where possible (Increased sales by 25%, Managed a team of 10, etc.)\n");
        prompt.append("- Use consistent date formatting (Month YYYY - Month YYYY)\n");
        prompt.append("- Maintain a professional tone throughout\n");
        prompt.append("- Format the resume in clean, readable text without any markdown or special formatting\n");
        prompt.append("- Ensure the resume is ATS-friendly with standard section headings\n");
        prompt.append("- Tailor the content to the ").append(jobTitle != null ? jobTitle : "professional")
                .append(" role\n");

        prompt.append("Final Output: Provide only the resume content without any additional explanations or comments.");

        return prompt.toString();
    }

    /**
     * Build prompt for resume generation (backward compatibility)
     * 
     * @param context  MCP context data
     * @param jobTitle Optional job title
     * @param format   Optional resume format (chronological, functional, hybrid)
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle, String format) {
        return buildResumePrompt(context, jobTitle, format, "professional");
    }

    /**
     * Format date string for resume display
     * 
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
                    String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
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
     * 
     * @param context  MCP context data
     * @param jobTitle Optional job title
     * @return Formatted prompt
     */
    private String buildResumePrompt(Map<String, Object> context, String jobTitle) {
        return buildResumePrompt(context, jobTitle, "chronological");
    }

    /**
     * Build prompt for job optimization
     * 
     * @param context MCP context data
     * @param jobId   Job ID
     * @return Formatted prompt
     */
    private String buildJobOptimizationPrompt(Map<String, Object> context, Integer jobId) {
        // For now, we'll use the same prompt structure but this can be enhanced
        // to include job description details when you implement job fetching
        return buildResumePrompt(context, null) +
                "\n\nPlease optimize this resume specifically for job ID: " + jobId;
    }

    /**
     * Call Mistral API
     * 
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
            System.out
                    .println("Response body length: " + (response.getBody() != null ? response.getBody().length() : 0));

            // Handle different response statuses
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "Error: Model '" + modelName + "' not found. Please check the model name in your configuration.";
            }

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "Error: Invalid Mistral API token. Please check your MISTRAL_API_TOKEN in .env file.";
            }

            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "Error: Access forbidden to Mistral model '" + modelName
                        + "'. This might be due to model permissions or rate limiting.";
            }

            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Error: Rate limit exceeded for Mistral API. Please try again later.";
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                return "Error calling Mistral API. Status: " + response.getStatusCode() + ". Response: "
                        + response.getBody();
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
                        return "Error: Model '" + modelName
                                + "' not found. Please check the model name in your configuration. Mistral API message: "
                                + errorMessage;
                    }
                    if (errorMessage.contains("Unauthorized") || errorMessage.contains("invalid")) {
                        return "Error: Invalid Mistral API token. Please check your MISTRAL_API_TOKEN in .env file. Mistral API message: "
                                + errorMessage;
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
            return "Error: Model '" + modelName
                    + "' not found (404). Please check the model name in your configuration.";
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("401 Unauthorized error when calling Mistral API");
            System.err.println("Token: " + (apiToken != null ? "Provided" : "Not provided"));
            e.printStackTrace();
            return "Error: Invalid Mistral API token (401). Please check your MISTRAL_API_TOKEN in .env file.";
        } catch (HttpClientErrorException.Forbidden e) {
            System.err.println("403 Forbidden error when calling Mistral API");
            e.printStackTrace();
            return "Error: Access forbidden to Mistral model '" + modelName
                    + "' (403). This might be due to model permissions or rate limiting.";
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
     * 
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

        return "==================== RESUME ====================\n\n" +
                "NAME: [User Name]\n" +
                "EMAIL: [User Email]\n" +
                "PHONE: [User Phone]\n" +
                "LOCATION: [User City, Country]\n\n" +
                "PROFESSIONAL SUMMARY:\n" +
                "Highly motivated and results-driven " + jobTitle
                + " with [X] years of experience in [industry/field]. " +
                "Proven track record of [key achievement or skill]. Seeking to leverage expertise in [relevant area] " +
                "to contribute to organizational success.\n\n" +
                "SKILLS:\n" +
                "TECHNICAL SKILLS:\n" +
                "- [Skill 1] (Proficiency: /10)\n" +
                "- [Skill 2] (Proficiency: /10)\n" +
                "- [Skill 3] (Proficiency: /10)\n\n" +
                "SOFT SKILLS:\n" +
                "- [Soft Skill 1]\n" +
                "- [Soft Skill 2]\n" +
                "- [Soft Skill 3]\n\n" +
                "PROFESSIONAL EXPERIENCE:\n" +
                "[Job Title]\n" +
                "[Company Name], [Location]\n" +
                "[Start Date] - [End Date]\n" +
                "Key Achievements:\n" +
                "- [Quantifiable achievement with metrics]\n" +
                "- [Project or initiative you led]\n" +
                "- [Result or improvement you delivered]\n\n" +
                "EDUCATION:\n" +
                "[Degree] in [Field of Study]\n" +
                "[University Name], [Location]\n" +
                "[Graduation Date]\n" +
                "[Relevant coursework or honors]\n\n" +
                "================================================\n\n" +
                "Note: This is a fallback resume template. To generate an AI-powered resume:\n" +
                "1. Ensure you have configured your Mistral API token in the .env file\n" +
                "2. Set MISTRAL_API_TOKEN=your_actual_token_here\n" +
                "3. Optionally set MISTRAL_MODEL_NAME to your preferred model\n" +
                "4. Restart the application\n\n" +
                "For more information, visit: https://mistral.ai/";
    }
}