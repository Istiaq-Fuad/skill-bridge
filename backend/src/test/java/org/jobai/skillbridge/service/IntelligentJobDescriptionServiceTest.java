package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.dto.AiResponseDto;
import org.jobai.skillbridge.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IntelligentJobDescriptionServiceTest {

    @Mock
    private MistralAiService mistralAiService;

    @InjectMocks
    private IntelligentJobDescriptionService intelligentJobDescriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateJobDescription() {
        // Given
        String jobTitle = "Software Engineer";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";
        
        // Mock AI response
        AiResponseDto aiResponse = new AiResponseDto();
        aiResponse.setSuccess(true);
        aiResponse.setContent("{\"jobTitle\": \"Software Engineer\", \"companyOverview\": \"Tech company\", \"jobSummary\": \"Exciting role\"}");
        
        when(mistralAiService.generateText(any(String.class), any(String.class)))
                .thenReturn(aiResponse);

        // When
        IntelligentJobDescriptionService.JobDescriptionGenerationResult result = 
                intelligentJobDescriptionService.generateJobDescription(jobTitle, industry, experienceLevel, location);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertEquals(location, result.getLocation());
        assertNotNull(result.getCompanyOverview());
        assertNotNull(result.getJobSummary());
        assertNotNull(result.getResponsibilities());
        assertNotNull(result.getRequiredQualifications());
        assertNotNull(result.getTechnicalSkills());
        assertNotNull(result.getProcessingTime());
    }

    @Test
    void testGenerateJobDescriptionWithEmptyTitle() {
        // Given
        String jobTitle = "";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";

        // When & Then
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            intelligentJobDescriptionService.generateJobDescription(jobTitle, industry, experienceLevel, location);
        });
        
        assertTrue(exception.getMessage().contains("Job title cannot be empty"));
    }

    @Test
    void testGenerateJobDescriptionWithNullTitle() {
        // Given
        String jobTitle = null;
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";

        // When & Then
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            intelligentJobDescriptionService.generateJobDescription(jobTitle, industry, experienceLevel, location);
        });
        
        assertTrue(exception.getMessage().contains("Job title cannot be empty"));
    }

    @Test
    void testOptimizeJobDescription() {
        // Given
        JobPost jobPost = createTestJobPost(1L);
        User employer = createTestUser(1L);
        jobPost.setEmployer(employer);
        
        // Mock AI response
        AiResponseDto aiResponse = new AiResponseDto();
        aiResponse.setSuccess(true);
        aiResponse.setContent("{\"titleOptimization\": \"Senior Software Engineer\", \"descriptionEnhancements\": [\"Add more details\"]}");
        
        when(mistralAiService.generateText(any(String.class), any(String.class)))
                .thenReturn(aiResponse);

        // When
        IntelligentJobDescriptionService.JobDescriptionOptimizationResult result = 
                intelligentJobDescriptionService.optimizeJobDescription(jobPost);

        // Then
        assertNotNull(result);
        assertEquals(jobPost.getPostId(), result.getOriginalJobId());
        assertNotNull(result.getTitleOptimization());
        assertNotNull(result.getDescriptionEnhancements());
        assertNotNull(result.getResponsibilityImprovements());
        assertNotNull(result.getProcessingTime());
    }

    @Test
    void testOptimizeJobDescriptionWithNullJob() {
        // Given
        JobPost jobPost = null;

        // When & Then
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            intelligentJobDescriptionService.optimizeJobDescription(jobPost);
        });
        
        assertTrue(exception.getMessage().contains("Existing job cannot be null"));
    }

    @Test
    void testSuggestSkills() {
        // Given
        String jobTitle = "Software Engineer";
        String industry = "Technology";
        String experienceLevel = "Mid";
        
        // Mock AI response
        AiResponseDto aiResponse = new AiResponseDto();
        aiResponse.setSuccess(true);
        aiResponse.setContent("{\"technicalSkills\": {\"programmingLanguages\": [\"Java\", \"Python\"]}}");
        
        when(mistralAiService.generateText(any(String.class), any(String.class)))
                .thenReturn(aiResponse);

        // When
        IntelligentJobDescriptionService.SkillSuggestionResult result = 
                intelligentJobDescriptionService.suggestSkills(jobTitle, industry, experienceLevel);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertNotNull(result.getTechnicalSkills());
        assertNotNull(result.getFrameworksAndTools());
        assertNotNull(result.getProcessingTime());
    }

    @Test
    void testSuggestSkillsWithEmptyTitle() {
        // Given
        String jobTitle = "";
        String industry = "Technology";
        String experienceLevel = "Mid";

        // When & Then
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            intelligentJobDescriptionService.suggestSkills(jobTitle, industry, experienceLevel);
        });
        
        assertTrue(exception.getMessage().contains("Job title cannot be empty"));
    }

    @Test
    void testSuggestSalaryRanges() {
        // Given
        String jobTitle = "Software Engineer";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";
        
        // Mock AI response
        AiResponseDto aiResponse = new AiResponseDto();
        aiResponse.setSuccess(true);
        aiResponse.setContent("{\"salaryRanges\": {\"currency\": \"BDT\", \"minimum\": 60000, \"midpoint\": 80000, \"maximum\": 120000}}");
        
        when(mistralAiService.generateText(any(String.class), any(String.class)))
                .thenReturn(aiResponse);

        // When
        IntelligentJobDescriptionService.SalarySuggestionResult result = 
                intelligentJobDescriptionService.suggestSalaryRanges(jobTitle, industry, experienceLevel, location);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertEquals(location, result.getLocation());
        assertNotNull(result.getSalaryRanges());
        assertNotNull(result.getMarketPositioning());
        assertNotNull(result.getProcessingTime());
    }

    @Test
    void testSuggestSalaryRangesWithEmptyTitle() {
        // Given
        String jobTitle = "";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";

        // When & Then
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            intelligentJobDescriptionService.suggestSalaryRanges(jobTitle, industry, experienceLevel, location);
        });
        
        assertTrue(exception.getMessage().contains("Job title cannot be empty"));
    }

    @Test
    void testBuildJobDescriptionPrompt() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("job_title", "Software Engineer");
        context.put("industry", "Technology");
        context.put("experience_level", "Mid");
        context.put("location", "Dhaka");

        // When
        // We'll use reflection to test the private method
        try {
            java.lang.reflect.Method method = IntelligentJobDescriptionService.class.getDeclaredMethod(
                    "buildJobDescriptionPrompt", Map.class);
            method.setAccessible(true);
            String prompt = (String) method.invoke(intelligentJobDescriptionService, context);
            
            // Then
            assertNotNull(prompt);
            assertTrue(prompt.contains("Software Engineer"));
            assertTrue(prompt.contains("Technology"));
            assertTrue(prompt.contains("Mid"));
            assertTrue(prompt.contains("Dhaka"));
            assertTrue(prompt.contains("JOB DETAILS"));
            assertTrue(prompt.contains("Please create a comprehensive job description"));
        } catch (Exception e) {
            fail("Failed to test buildJobDescriptionPrompt: " + e.getMessage());
        }
    }

    @Test
    void testBuildJobOptimizationPrompt() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("job_title", "Software Engineer");
        context.put("job_description", "Develop software applications");
        context.put("required_experience", 3);
        context.put("tech_stack", Arrays.asList("Java", "Spring"));
        context.put("location", "Dhaka");
        context.put("employment_type", "FULL_TIME");
        context.put("salary_min", 60000);
        context.put("salary_max", 100000);

        // When
        // We'll use reflection to test the private method
        try {
            java.lang.reflect.Method method = IntelligentJobDescriptionService.class.getDeclaredMethod(
                    "buildJobOptimizationPrompt", Map.class);
            method.setAccessible(true);
            String prompt = (String) method.invoke(intelligentJobDescriptionService, context);
            
            // Then
            assertNotNull(prompt);
            assertTrue(prompt.contains("Software Engineer"));
            assertTrue(prompt.contains("Develop software applications"));
            assertTrue(prompt.contains("Java"));
            assertTrue(prompt.contains("Dhaka"));
            assertTrue(prompt.contains("EXISTING JOB DESCRIPTION"));
            assertTrue(prompt.contains("Please analyze this job description"));
        } catch (Exception e) {
            fail("Failed to test buildJobOptimizationPrompt: " + e.getMessage());
        }
    }

    @Test
    void testBuildSkillSuggestionPrompt() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("job_title", "Software Engineer");
        context.put("industry", "Technology");
        context.put("experience_level", "Mid");

        // When
        // We'll use reflection to test the private method
        try {
            java.lang.reflect.Method method = IntelligentJobDescriptionService.class.getDeclaredMethod(
                    "buildSkillSuggestionPrompt", Map.class);
            method.setAccessible(true);
            String prompt = (String) method.invoke(intelligentJobDescriptionService, context);
            
            // Then
            assertNotNull(prompt);
            assertTrue(prompt.contains("Software Engineer"));
            assertTrue(prompt.contains("Technology"));
            assertTrue(prompt.contains("Mid"));
            assertTrue(prompt.contains("JOB ROLE"));
            assertTrue(prompt.contains("Please provide skill suggestions"));
        } catch (Exception e) {
            fail("Failed to test buildSkillSuggestionPrompt: " + e.getMessage());
        }
    }

    @Test
    void testBuildSalarySuggestionPrompt() {
        // Given
        Map<String, Object> context = new HashMap<>();
        context.put("job_title", "Software Engineer");
        context.put("industry", "Technology");
        context.put("experience_level", "Mid");
        context.put("location", "Dhaka");

        // When
        // We'll use reflection to test the private method
        try {
            java.lang.reflect.Method method = IntelligentJobDescriptionService.class.getDeclaredMethod(
                    "buildSalarySuggestionPrompt", Map.class);
            method.setAccessible(true);
            String prompt = (String) method.invoke(intelligentJobDescriptionService, context);
            
            // Then
            assertNotNull(prompt);
            assertTrue(prompt.contains("Software Engineer"));
            assertTrue(prompt.contains("Technology"));
            assertTrue(prompt.contains("Mid"));
            assertTrue(prompt.contains("Dhaka"));
            assertTrue(prompt.contains("JOB ROLE"));
            assertTrue(prompt.contains("Please provide salary range suggestions"));
        } catch (Exception e) {
            fail("Failed to test buildSalarySuggestionPrompt: " + e.getMessage());
        }
    }

    // Helper methods to create test data
    private JobPost createTestJobPost(Long id) {
        JobPost job = new JobPost();
        job.setPostId(id);
        job.setPostProfile("Software Engineer");
        job.setPostDesc("Develop software applications");
        job.setReqExperience(3);
        job.setPostTechStack(Arrays.asList("Java", "Spring"));
        job.setLocation("Dhaka");
        job.setEmploymentType("FULL_TIME");
        job.setSalaryMin(60000.0);
        job.setSalaryMax(100000.0);
        job.setSalaryCurrency("BDT");
        return job;
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("employer" + id);
        user.setRole(org.jobai.skillbridge.model.UserRole.EMPLOYER);
        return user;
    }
}