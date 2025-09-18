package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnhancedAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JobRepo jobRepository;
    
    @Mock
    private JobApplicationRepository applicationRepository;
    
    @Mock
    private AdminProfileRepository adminProfileRepository;
    
    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private EnhancedAdminService enhancedAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPlatformStatistics() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(UserRole.JOB_SEEKER)).thenReturn(70L);
        when(userRepository.countByRole(UserRole.EMPLOYER)).thenReturn(25L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(5L);
        when(jobRepository.count()).thenReturn(50L);
        when(jobRepository.countByJobStatus("ACTIVE")).thenReturn(40L);
        when(jobRepository.countByJobStatus("FILLED")).thenReturn(5L);
        when(jobRepository.countByJobStatus("EXPIRED")).thenReturn(5L);
        when(applicationRepository.count()).thenReturn(200L);
        when(applicationRepository.countByStatus("APPLIED")).thenReturn(150L);
        when(applicationRepository.countByStatus("ACCEPTED")).thenReturn(30L);
        when(applicationRepository.countByStatus("REJECTED")).thenReturn(20L);

        // When
        EnhancedAdminService.PlatformStatistics stats = enhancedAdminService.getPlatformStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(70L, stats.getTotalJobSeekers());
        assertEquals(25L, stats.getTotalEmployers());
        assertEquals(5L, stats.getTotalAdmins());
        assertEquals(50L, stats.getTotalJobs());
        assertEquals(40L, stats.getActiveJobs());
        assertEquals(10L, stats.getClosedJobs());
        assertEquals(200L, stats.getTotalApplications());
        assertEquals(150L, stats.getPendingApplications());
        assertEquals(30L, stats.getAcceptedApplications());
        assertEquals(20L, stats.getRejectedApplications());
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(createTestUser(1L), createTestUser(2L), createTestUser(3L));
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = enhancedAdminService.getAllUsers(0, 2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetUserById() {
        // Given
        Long userId = 1L;
        User user = createTestUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = enhancedAdminService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void testUpdateUserAccountStatus() {
        // Given
        Long userId = 1L;
        User user = createTestUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = enhancedAdminService.updateUserAccountStatus(userId, false);

        // Then
        assertNotNull(result);
        assertFalse(result.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserAccountStatusWithNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enhancedAdminService.updateUserAccountStatus(userId, false);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAllJobs() {
        // Given
        List<JobPost> jobs = Arrays.asList(createTestJob(1L), createTestJob(2L), createTestJob(3L));
        when(jobRepository.findAll()).thenReturn(jobs);

        // When
        List<JobPost> result = enhancedAdminService.getAllJobs(0, 2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateJobStatus() {
        // Given
        Long jobId = 1L;
        JobPost job = createTestJob(jobId);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(JobPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobPost result = enhancedAdminService.updateJobStatus(jobId, "INACTIVE");

        // Then
        assertNotNull(result);
        assertEquals("INACTIVE", result.getJobStatus());
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    void testUpdateJobStatusWithNonExistentJob() {
        // Given
        Long jobId = 999L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enhancedAdminService.updateJobStatus(jobId, "INACTIVE");
        });
        
        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void testGetAllApplications() {
        // Given
        List<JobApplication> applications = Arrays.asList(
                createTestApplication(1L), 
                createTestApplication(2L), 
                createTestApplication(3L));
        when(applicationRepository.findAll()).thenReturn(applications);

        // When
        List<JobApplication> result = enhancedAdminService.getAllApplications(0, 2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testUpdateApplicationStatus() {
        // Given
        Long applicationId = 1L;
        JobApplication application = createTestApplication(applicationId);
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobApplication result = enhancedAdminService.updateApplicationStatus(applicationId, "REVIEWED");

        // Then
        assertNotNull(result);
        assertEquals("REVIEWED", result.getStatus());
        verify(applicationRepository, times(1)).save(application);
    }

    @Test
    void testUpdateApplicationStatusWithNonExistentApplication() {
        // Given
        Long applicationId = 999L;
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enhancedAdminService.updateApplicationStatus(applicationId, "REVIEWED");
        });
        
        assertEquals("Application not found", exception.getMessage());
    }

    // Helper methods to create test data
    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(UserRole.JOB_SEEKER);
        user.setActive(true);
        return user;
    }

    private JobPost createTestJob(Long id) {
        JobPost job = new JobPost();
        job.setPostId(id);
        job.setPostProfile("Software Engineer");
        job.setJobStatus("ACTIVE");
        return job;
    }

    private JobApplication createTestApplication(Long id) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setStatus("APPLIED");
        return application;
    }
}