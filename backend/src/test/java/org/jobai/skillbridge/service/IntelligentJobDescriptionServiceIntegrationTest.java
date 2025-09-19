package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.JobPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IntelligentJobDescriptionServiceIntegrationTest {

    @Autowired
    private IntelligentJobDescriptionService intelligentJobDescriptionService;

    /**
     * Integration test to verify that the IntelligentJobDescriptionService can generate job descriptions
     * This test demonstrates the actual functionality of the service
     */
    @Test
    void testGenerateJobDescriptionIntegration() {
        // Given
        String jobTitle = "Software Engineer";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";

        // When
        IntelligentJobDescriptionService.JobDescriptionGenerationResult result = 
                intelligentJobDescriptionService.generateJobDescription(jobTitle, industry, experienceLevel, location);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertEquals(location, result.getLocation());
        assertNotNull(result.getProcessingTime());
        
        // Verify that we get actual content, not just placeholder data
        assertNotNull(result.getCompanyOverview());
        assertNotNull(result.getJobSummary());
        assertNotNull(result.getResponsibilities());
        assertNotNull(result.getRequiredQualifications());
        assertNotNull(result.getTechnicalSkills());
        assertNotNull(result.getSoftSkills());
        assertNotNull(result.getBenefits());
        assertNotNull(result.getWorkEnvironment());
        
        // Verify that we get meaningful content
        assertFalse(result.getCompanyOverview().isEmpty());
        assertFalse(result.getJobSummary().isEmpty());
        assertFalse(result.getResponsibilities().isEmpty());
        assertFalse(result.getRequiredQualifications().isEmpty());
        assertFalse(result.getTechnicalSkills().isEmpty());
        assertFalse(result.getSoftSkills().isEmpty());
        assertFalse(result.getBenefits().isEmpty());
        assertFalse(result.getWorkEnvironment().isEmpty());
    }

    /**
     * Integration test to verify that the IntelligentJobDescriptionService can suggest skills
     * This test demonstrates the actual functionality of the service
     */
    @Test
    void testSuggestSkillsIntegration() {
        // Given
        String jobTitle = "Data Scientist";
        String industry = "Technology";
        String experienceLevel = "Senior";

        // When
        IntelligentJobDescriptionService.SkillSuggestionResult result = 
                intelligentJobDescriptionService.suggestSkills(jobTitle, industry, experienceLevel);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertNotNull(result.getProcessingTime());
        
        // Verify that we get actual content, not just placeholder data
        assertNotNull(result.getTechnicalSkills());
        assertNotNull(result.getFrameworksAndTools());
        assertNotNull(result.getDomainKnowledge());
        assertNotNull(result.getCertifications());
        assertNotNull(result.getEmergingTechnologies());
        assertNotNull(result.getSoftSkills());
        
        // Verify that we get meaningful content
        assertFalse(result.getTechnicalSkills().isEmpty());
        assertFalse(result.getFrameworksAndTools().isEmpty());
        assertFalse(result.getDomainKnowledge().isEmpty());
        assertFalse(result.getCertifications().isEmpty());
        assertFalse(result.getEmergingTechnologies().isEmpty());
        assertFalse(result.getSoftSkills().isEmpty());
    }

    /**
     * Integration test to verify that the IntelligentJobDescriptionService can suggest salary ranges
     * This test demonstrates the actual functionality of the service
     */
    @Test
    void testSuggestSalaryRangesIntegration() {
        // Given
        String jobTitle = "Product Manager";
        String industry = "Technology";
        String experienceLevel = "Mid";
        String location = "Dhaka";

        // When
        IntelligentJobDescriptionService.SalarySuggestionResult result = 
                intelligentJobDescriptionService.suggestSalaryRanges(jobTitle, industry, experienceLevel, location);

        // Then
        assertNotNull(result);
        assertEquals(jobTitle, result.getJobTitle());
        assertEquals(industry, result.getIndustry());
        assertEquals(experienceLevel, result.getExperienceLevel());
        assertEquals(location, result.getLocation());
        assertNotNull(result.getProcessingTime());
        
        // Verify that we get actual content, not just placeholder data
        assertNotNull(result.getSalaryRanges());
        assertNotNull(result.getMarketPositioning());
        assertNotNull(result.getBenefitsPackage());
        assertNotNull(result.getPerformanceIncentives());
        
        // Verify salary ranges
        assertNotNull(result.getSalaryRanges().getCurrency());
        assertTrue(result.getSalaryRanges().getMinimum() > 0);
        assertTrue(result.getSalaryRanges().getMidpoint() > result.getSalaryRanges().getMinimum());
        assertTrue(result.getSalaryRanges().getMaximum() > result.getSalaryRanges().getMidpoint());
        
        // Verify that we get meaningful content
        assertFalse(result.getMarketPositioning().isEmpty());
        assertFalse(result.getBenefitsPackage().isEmpty());
        assertFalse(result.getPerformanceIncentives().isEmpty());
    }
}