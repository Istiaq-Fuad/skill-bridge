package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.Education;
import org.jobai.skillbridge.model.Experience;
import org.jobai.skillbridge.model.Skill;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.FileStorage;
import org.jobai.skillbridge.repo.FileStorageRepository;
import org.jobai.skillbridge.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private TebiFileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private MistralAiService mistralAiService;

    /**
     * Parse a resume file and extract structured data
     * 
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
     * 
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
     * 
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

    private String extractName(String content) {
        // Look for name patterns at the beginning of the document
        String[] lines = content.split("\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            // Skip empty lines and lines with too many special characters
            if (line.length() > 3 && line.length() < 50 &&
                    !line.contains("@") && !line.contains("www") &&
                    line.matches(".*[a-zA-Z].*")) {
                return line;
            }
        }
        return "N/A";
    }

    private String extractEmail(String content) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractPhone(String content) {
        Pattern phonePattern = Pattern.compile("(?:\\+?1[-\\s]?)?\\(?([0-9]{3})\\)?[-\\s]?([0-9]{3})[-\\s]?([0-9]{4})");
        Matcher matcher = phonePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractSummary(String content) {
        // Look for sections that might contain summary
        String[] summaryKeywords = { "summary", "objective", "profile", "about" };
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase();
            for (String keyword : summaryKeywords) {
                if (line.contains(keyword) && line.length() < 50) {
                    // Found a summary section, extract next few lines
                    StringBuilder summary = new StringBuilder();
                    for (int j = i + 1; j < Math.min(i + 5, lines.length); j++) {
                        String summaryLine = lines[j].trim();
                        if (summaryLine.length() > 20) {
                            summary.append(summaryLine).append(" ");
                        }
                    }
                    if (summary.length() > 0) {
                        return summary.toString().trim();
                    }
                }
            }
        }
        return null;
    }

    private List<Skill> extractSkills(String content) {
        List<Skill> skills = new ArrayList<>();

        // Common technical skills to look for
        String[] techSkills = {
                "Java", "Python", "JavaScript", "React", "Angular", "Vue", "Spring", "Node.js",
                "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Docker", "Kubernetes",
                "AWS", "Azure", "GCP", "Git", "Jenkins", "Maven", "Gradle", "Linux"
        };

        String contentLower = content.toLowerCase();

        for (String skill : techSkills) {
            if (contentLower.contains(skill.toLowerCase())) {
                Skill skillObj = new Skill();
                skillObj.setName(skill);
                skillObj.setCategory("Technical");
                skillObj.setLevel(org.jobai.skillbridge.model.SkillLevel.INTERMEDIATE); // Default proficiency
                skills.add(skillObj);
            }
        }

        return skills;
    }

    private List<Experience> extractExperiences(String content) {
        List<Experience> experiences = new ArrayList<>();

        String[] lines = content.split("\n");
        boolean inExperienceSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase().trim();

            // Check if we're entering an experience section
            if (line.contains("experience") || line.contains("employment") || line.contains("work")) {
                inExperienceSection = true;
                continue;
            }

            // Check if we're leaving the experience section
            if (inExperienceSection && (line.contains("education") || line.contains("skills") ||
                    line.contains("projects") || line.contains("certifications"))) {
                break;
            }

            if (inExperienceSection && lines[i].trim().length() > 0) {
                // Try to extract experience entry
                Experience exp = extractExperienceEntry(lines, i);
                if (exp != null) {
                    experiences.add(exp);
                }
            }
        }

        return experiences;
    }

    private Experience extractExperienceEntry(String[] lines, int startIndex) {
        if (startIndex >= lines.length)
            return null;

        String line = lines[startIndex].trim();

        // Look for patterns like "Company Name - Position" or "Position at Company"
        if (line.length() > 10 && (line.contains("-") || line.contains("at"))) {
            Experience exp = new Experience();

            if (line.contains("-")) {
                String[] parts = line.split("-", 2);
                if (parts.length == 2) {
                    exp.setCompany(parts[0].trim());
                    exp.setPosition(parts[1].trim());
                }
            } else if (line.contains(" at ")) {
                String[] parts = line.split(" at ", 2);
                if (parts.length == 2) {
                    exp.setPosition(parts[0].trim());
                    exp.setCompany(parts[1].trim());
                }
            }

            // Extract description from next few lines
            StringBuilder description = new StringBuilder();
            for (int i = startIndex + 1; i < Math.min(startIndex + 4, lines.length); i++) {
                String descLine = lines[i].trim();
                if (descLine.length() > 20 && !descLine.toLowerCase().contains("experience") &&
                        !descLine.toLowerCase().contains("education")) {
                    description.append(descLine).append(" ");
                }
            }

            exp.setDescription(description.toString().trim());
            exp.setStartDate(LocalDate.now().minusYears(2)); // Default dates
            exp.setEndDate(LocalDate.now());
            exp.setCurrentlyWorking(false);

            return exp;
        }

        return null;
    }

    private List<Education> extractEducations(String content) {
        List<Education> educations = new ArrayList<>();

        String[] lines = content.split("\n");
        boolean inEducationSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].toLowerCase().trim();

            // Check if we're entering an education section
            if (line.contains("education") || line.contains("academic") || line.contains("qualification")) {
                inEducationSection = true;
                continue;
            }

            // Check if we're leaving the education section
            if (inEducationSection && (line.contains("experience") || line.contains("skills") ||
                    line.contains("projects") || line.contains("certifications"))) {
                break;
            }

            if (inEducationSection && lines[i].trim().length() > 0) {
                // Try to extract education entry
                Education edu = extractEducationEntry(lines, i);
                if (edu != null) {
                    educations.add(edu);
                }
            }
        }

        return educations;
    }

    private Education extractEducationEntry(String[] lines, int startIndex) {
        if (startIndex >= lines.length)
            return null;

        String line = lines[startIndex].trim();

        // Look for degree patterns
        String[] degreeTypes = { "bachelor", "master", "phd", "mba", "bs", "ms", "ba", "ma" };

        for (String degreeType : degreeTypes) {
            if (line.toLowerCase().contains(degreeType)) {
                Education edu = new Education();
                edu.setDegree(line);

                // Look for institution in the same line or next line
                if (startIndex + 1 < lines.length) {
                    String nextLine = lines[startIndex + 1].trim();
                    if (nextLine.length() > 5 && nextLine.toLowerCase().contains("university") ||
                            nextLine.toLowerCase().contains("college")
                            || nextLine.toLowerCase().contains("institute")) {
                        edu.setInstitution(nextLine);
                    }
                }

                edu.setStartDate(LocalDate.now().minusYears(4)); // Default dates
                edu.setEndDate(LocalDate.now());

                return edu;
            }
        }

        return null;
    }

    /**
     * Parsed resume data structure
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
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<Skill> getSkills() {
            return skills;
        }

        public void setSkills(List<Skill> skills) {
            this.skills = skills;
        }

        public List<Experience> getExperiences() {
            return experiences;
        }

        public void setExperiences(List<Experience> experiences) {
            this.experiences = experiences;
        }

        public List<Education> getEducations() {
            return educations;
        }

        public void setEducations(List<Education> educations) {
            this.educations = educations;
        }
    }

    /**
     * Parse resume with file storage integration
     * 
     * @param file The uploaded resume file
     * @param user The user who uploaded the resume
     * @return Enhanced parsing result with file storage
     */
    public EnhancedParseResult parseResumeWithStorage(MultipartFile file, User user)
            throws IOException, SAXException, AiServiceException {
        // Validate file type
        if (!fileStorageService.isFileTypeAllowed(file, TebiFileStorageService.RESUME_ALLOWED_TYPES)) {
            throw new AiServiceException("Invalid file type. Only PDF, DOC, and DOCX files are allowed.");
        }

        // Upload to Tebi
        String fileUrl = fileStorageService.uploadResume(file, user.getId());
        String filePath = fileStorageService.extractFilePathFromUrl(fileUrl);

        // Save file metadata
        FileStorage fileStorage = new FileStorage(
                file.getName(),
                file.getOriginalFilename(),
                fileUrl,
                filePath,
                file.getContentType(),
                file.getSize(),
                FileStorage.FileCategory.RESUME,
                user);
        fileStorageRepository.save(fileStorage);

        // Parse the resume
        ParsedResumeData parsedData = parseResume(file);

        // Return enhanced result
        EnhancedParseResult result = new EnhancedParseResult();
        result.setParsedData(parsedData);
        result.setFileStorage(fileStorage);
        result.setFileUrl(fileUrl);

        return result;
    }

    /**
     * Get user's latest resume
     */
    public Optional<FileStorage> getLatestResume(User user) {
        return fileStorageRepository.findFirstByUserAndCategoryAndIsActiveOrderByUploadedAtDesc(
                user, FileStorage.FileCategory.RESUME, true);
    }

    /**
     * Get all resumes for a user
     */
    public List<FileStorage> getUserResumes(User user) {
        return fileStorageRepository.findByUserAndCategoryAndIsActive(
                user, FileStorage.FileCategory.RESUME, true);
    }

    /**
     * Delete resume file
     */
    public void deleteResume(Long fileId, User user) {
        Optional<FileStorage> fileStorage = fileStorageRepository.findById(fileId);
        if (fileStorage.isPresent() && fileStorage.get().getUser().getId().equals(user.getId())) {
            FileStorage file = fileStorage.get();

            // Delete from Tebi
            fileStorageService.deleteFile(file.getFilePath());

            // Mark as inactive in database
            file.setActive(false);
            fileStorageRepository.save(file);
        }
    }

    /**
     * Enhanced parsing result class
     */
    public static class EnhancedParseResult {
        private ParsedResumeData parsedData;
        private FileStorage fileStorage;
        private String fileUrl;

        public ParsedResumeData getParsedData() {
            return parsedData;
        }

        public void setParsedData(ParsedResumeData parsedData) {
            this.parsedData = parsedData;
        }

        public FileStorage getFileStorage() {
            return fileStorage;
        }

        public void setFileStorage(FileStorage fileStorage) {
            this.fileStorage = fileStorage;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
    }
}