package org.jobai.skillbridge.service;

import org.jobai.skillbridge.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeParsingServiceTest {

    @InjectMocks
    private ResumeParsingService resumeParsingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseResumeWithEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // When & Then
        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> resumeParsingService.parseResume(emptyFile)
        );
        
        assertEquals("Uploaded file is empty", exception.getMessage());
    }

    @Test
    void testParseResumeWithInvalidFileType() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "This is a text file".getBytes()
        );

        // When & Then
        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> resumeParsingService.parseResume(invalidFile)
        );
        
        assertTrue(exception.getMessage().contains("Unsupported file type"));
    }
}