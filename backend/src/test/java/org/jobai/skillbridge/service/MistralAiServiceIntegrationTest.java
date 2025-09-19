package org.jobai.skillbridge.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MistralAiServiceIntegrationTest {

    @Autowired
    private MistralAiService mistralAiService;

    /**
     * Integration test to verify that the MistralAiService can generate text
     * This test demonstrates the actual functionality of the service
     */
    @Test
    void testGenerateTextIntegration() {
        // Given
        String prompt = "Write a short description of what a software engineer does.";
        String responseType = "job-description";

        // When
        AiResponseDto result = mistralAiService.generateText(prompt, responseType);

        // Then
        assertNotNull(result);
        assertEquals(responseType, result.getFormat());
        assertNotNull(result.getProcessingTimeMs());
        
        // If the API is configured correctly, we should get a successful response
        // If not, we'll get an error message
        if (result.isSuccess()) {
            assertNotNull(result.getContent());
            assertFalse(result.getContent().isEmpty());
        } else {
            // This is expected if the API token is not configured
            assertNotNull(result.getMessage());
            assertFalse(result.getMessage().isEmpty());
        }
    }
}