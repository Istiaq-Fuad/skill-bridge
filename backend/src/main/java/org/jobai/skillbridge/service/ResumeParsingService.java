package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

@Service
public class ResumeParsingService {

    private final Tika tika = new Tika();

    /**
     * Parse a resume file and extract structured data
     * @param file The uploaded resume file
     * @return Parsed resume data
     */
    public ParsedResumeData parseResume(MultipartFile file) throws IOException, SAXException, AiServiceException {
        if (file.isEmpty()) {
            throw new AiServiceException("Uploaded file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && 
                                   !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") &&
                                   !contentType.equals("application/msword"))) {
            throw new AiServiceException("Unsupported file type. Please upload PDF or DOCX files.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String content = extractTextFromDocument(inputStream, contentType);
            return extractStructuredData(content);
        } catch (Exception e) {
            throw new AiServiceException("Error parsing resume: " + e.getMessage());
        }
    }

    /**
     * Extract text content from document based on file type
     * @param inputStream Input stream of the document
     * @param contentType MIME type of the document
     * @return Extracted text content
     */
    private String extractTextFromDocument(InputStream inputStream, String contentType) 
            throws IOException, SAXException, AiServiceException {
        try {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 to avoid truncation
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            parser.parse(inputStream, handler, metadata, context);
            return handler.toString().trim();
        } catch (Exception e) {
            throw new AiServiceException("Failed to extract text from document: " + e.getMessage());
        }
    }

    /**
     * Extract structured data from resume text
     * @param content Resume text content
     * @return Structured resume data
     */
    private ParsedResumeData extractStructuredData(String content) {
        ParsedResumeData data = new ParsedResumeData();
        
        // Extract contact information
        data.setName(extractName(content));
        data.setEmail(extractEmail(content));
        data.setPhone(extractPhone(content));
        
        // Extract professional summary
        data.setSummary(extractSummary(content));
        
        // Extract skills
        data.setSkills(extractSkills(content));
        
        // Extract experience
        data.setExperiences(extractExperiences(content));
        
        // Extract education
        data.setEducations(extractEducations(content));
        
        return data;
    }

    /**
     * Extract name from resume content
     * @param content Resume content
     * @return Extracted name
     */
    private String extractName(String content) {
        // Look for name at the beginning of the resume (first few lines)
        String[] lines = content.split("\\r?\\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // Simple heuristic: if first line is not an email or phone, assume it's the name
            if (!firstLine.contains("@") && !firstLine.matches(".*\\d{3}.*")) {
                return firstLine;
            }
        }
        return "Unknown";
    }

    /**
     * Extract email from resume content
     * @param content Resume content
     * @return Extracted email
     */
    private String extractEmail(String content) {
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = emailPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Extract phone number from resume content
     * @param content Resume content
     * @return Extracted phone number
     */
    private String extractPhone(String content) {
        // Match various phone number formats
        Pattern phonePattern = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
        Matcher matcher = phonePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    /**
     * Extract professional summary from resume content
     * @param content Resume content
     * @return Extracted summary
     */
    private String extractSummary(String content) {
        // Look for common summary section headers
        String[] summaryHeaders = {"SUMMARY", "PROFESSIONAL SUMMARY", "PROFILE", "CAREER SUMMARY"};
        
        for (String header : summaryHeaders) {
            int startIndex = content.indexOf(header);
            if (startIndex != -1) {
                // Find the end of the summary section (next section header or end of content)
                int endIndex = content.indexOf("\n\n", startIndex);
                if (endIndex == -1) {
                    endIndex = content.length();
                }
                
                String summary = content.substring(startIndex + header.length(), endIndex).trim();
                // Clean up common artifacts
                summary = summary.replaceAll("^[:\\-\\s]*", "").trim();
                if (!summary.isEmpty()) {
                    return summary;
                }
            }
        }
        
        return null;
    }

    /**
     * Extract skills from resume content
     * @param content Resume content
     * @return List of extracted skills
     */
    private List<Skill> extractSkills(String content) {
        List<Skill> skills = new ArrayList<>();
        
        // Look for skills section
        String[] skillsHeaders = {"SKILLS", "TECHNICAL SKILLS", "CORE COMPETENCIES", "EXPERTISE"};
        
        for (String header : skillsHeaders) {
            int startIndex = content.indexOf(header);
            if (startIndex != -1) {
                // Extract skills until next section or end of content
                int endIndex = content.indexOf("\n\n", startIndex);
                if (endIndex == -1) {
                    endIndex = content.length();
                }
                
                String skillsSection = content.substring(startIndex + header.length(), endIndex);
                
                // Simple approach: split by common delimiters and extract skills
                String[] potentialSkills = skillsSection.split("[,\\n•\\-]");
                
                for (String skillStr : potentialSkills) {
                    String cleanSkill = skillStr.trim()
                        .replaceAll("^[0-9]+\\.\\s*", "") // Remove numbered lists
                        .replaceAll("\\([^)]*\\)", "") // Remove parenthetical content
                        .trim();
                    
                    if (!cleanSkill.isEmpty() && cleanSkill.length() > 1) {
                        // Create skill with default proficiency level
                        Skill skill = new Skill();
                        skill.setName(cleanSkill);
                        skill.setCategory("General"); // Will be refined later
                        skill.setProficiencyLevel(5); // Default level
                        skills.add(skill);
                    }
                }
                
                break; // Found skills section
            }
        }
        
        return skills;
    }

    /**
     * Extract experiences from resume content
     * @param content Resume content
     * @return List of extracted experiences
     */
    private List<Experience> extractExperiences(String content) {
        List<Experience> experiences = new ArrayList<>();
        
        // Look for experience section
        String[] experienceHeaders = {"EXPERIENCE", "WORK EXPERIENCE", "EMPLOYMENT HISTORY", "PROFESSIONAL EXPERIENCE"};
        
        for (String header : experienceHeaders) {
            int startIndex = content.indexOf(header);
            if (startIndex != -1) {
                // Extract experience section
                int endIndex = content.indexOf("\n\n", startIndex + header.length());
                if (endIndex == -1) {
                    endIndex = content.length();
                }
                
                String experienceSection = content.substring(startIndex + header.length(), endIndex);
                
                // Simple approach: look for company/position patterns
                // This is a simplified implementation - a full implementation would be more sophisticated
                String[] entries = experienceSection.split("\\n\\s*\\n"); // Split by double newlines
                
                for (String entry : entries) {
                    if (entry.trim().isEmpty()) continue;
                    
                    Experience exp = new Experience();
                    // Extract position (first line)
                    String[] lines = entry.split("\\r?\\n");
                    if (lines.length > 0) {
                        exp.setPosition(lines[0].trim());
                    }
                    
                    // Extract company (look for patterns like "at Company" or "Company, Location")
                    if (lines.length > 1) {
                        String companyLine = lines[1].trim();
                        if (companyLine.toLowerCase().contains("at ")) {
                            exp.setCompany(companyLine.substring(companyLine.toLowerCase().indexOf("at ") + 3).trim());
                        } else {
                            exp.setCompany(companyLine);
                        }
                    }
                    
                    // Set default dates
                    exp.setStartDate(LocalDate.now().minusYears(1));
                    exp.setEndDate(LocalDate.now());
                    exp.setCurrentlyWorking(false);
                    
                    experiences.add(exp);
                }
                
                break; // Found experience section
            }
        }
        
        return experiences;
    }

    /**
     * Extract educations from resume content
     * @param content Resume content
     * @return List of extracted educations
     */
    private List<Education> extractEducations(String content) {
        List<Education> educations = new ArrayList<>();
        
        // Look for education section
        String[] educationHeaders = {"EDUCATION", "EDUCATIONAL BACKGROUND", "ACADEMIC QUALIFICATIONS"};
        
        for (String header : educationHeaders) {
            int startIndex = content.indexOf(header);
            if (startIndex != -1) {
                // Extract education section
                int endIndex = content.indexOf("\n\n", startIndex + header.length());
                if (endIndex == -1) {
                    endIndex = content.length();
                }
                
                String educationSection = content.substring(startIndex + header.length(), endIndex);
                
                // Simple approach: look for degree patterns
                String[] entries = educationSection.split("\\n\\s*\\n");
                
                for (String entry : entries) {
                    if (entry.trim().isEmpty()) continue;
                    
                    Education edu = new Education();
                    
                    // Extract degree and institution
                    String[] lines = entry.split("\\r?\\n");
                    if (lines.length > 0) {
                        // Look for degree keywords
                        String degreeLine = lines[0].trim();
                        edu.setDegree(degreeLine);
                        
                        // Try to extract field of study
                        if (degreeLine.toLowerCase().contains(" in ")) {
                            int inIndex = degreeLine.toLowerCase().indexOf(" in ");
                            edu.setFieldOfStudy(degreeLine.substring(inIndex + 4).trim());
                        }
                    }
                    
                    if (lines.length > 1) {
                        edu.setInstitution(lines[1].trim());
                    }
                    
                    // Set default dates
                    edu.setStartDate(LocalDate.now().minusYears(4));
                    edu.setEndDate(LocalDate.now());
                    
                    educations.add(edu);
                }
                
                break; // Found education section
            }
        }
        
        return educations;
    }

    /**
     * DTO for parsed resume data
     */
    public static class ParsedResumeData {
        private String name;
        private String email;
        private String phone;
        private String summary;
        private List<Skill> skills = new ArrayList<>();
        private List<Experience> experiences = new ArrayList<>();
        private List<Education> educations = new ArrayList<>();

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public List<Skill> getSkills() { return skills; }
        public void setSkills(List<Skill> skills) { this.skills = skills; }
        
        public List<Experience> getExperiences() { return experiences; }
        public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }
        
        public List<Education> getEducations() { return educations; }
        public void setEducations(List<Education> educations) { this.educations = educations; }
    }
}