package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnhancedEmployerDashboardServiceTest {

    @Mock
    private JobService jobService;
    
    @Mock
    private ApplicationService applicationService;
    
    @Mock
    private JobRepo jobRepository;
    
    @Mock
    private JobApplicationRepository applicationRepository;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnhancedEmployerDashboardService enhancedEmployerDashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetEnhancedDashboardStats() {
        // Given
        User employer = createTestUser(1L);
        List<JobPost> jobs = Arrays.asList(createTestJob(1L), createTestJob(2L));
        List<JobApplication> applications = Arrays.asList(
                createTestApplication(1L, jobs.get(0)), 
                createTestApplication(2L, jobs.get(1))
        );
        
        when(jobService.getJobsByEmployer(employer)).thenReturn(jobs);
        when(applicationService.getApplicationsForEmployer(employer)).thenReturn(applications);
        when(applicationRepository.findByJobPost(any(JobPost.class))).thenReturn(new ArrayList<>());

        // When
        EnhancedEmployerDashboardService.EnhancedDashboardStats stats = 
                enhancedEmployerDashboardService.getEnhancedDashboardStats(employer);

        // Then
        assertNotNull(stats);
        assertEquals(2, stats.getTotalJobs());
        assertEquals(2, stats.getTotalApplications());
        assertNotNull(stats.getApplicationsByStatus());
        assertNotNull(stats.getRecentJobs());
        assertNotNull(stats.getRecentApplications());
        assertNotNull(stats.getJobPerformanceMetrics());
        assertNotNull(stats.getApplicationTrends());
    }

    @Test
    void testGetJobAnalytics() {
        // Given
        Long jobId = 1L;
        User employer = createTestUser(1L);
        JobPost job = createTestJob(jobId);
        job.setEmployer(employer);
        List<JobApplication> applications = Arrays.asList(
                createTestApplication(1L, job), 
                createTestApplication(2L, job)
        );
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobPost(job)).thenReturn(applications);

        // When
        EnhancedEmployerDashboardService.JobAnalytics analytics = 
                enhancedEmployerDashboardService.getJobAnalytics(jobId, employer);

        // Then
        assertNotNull(analytics);
        assertEquals(job, analytics.getJob());
        assertEquals(2, analytics.getTotalApplications());
        assertNotNull(analytics.getApplicationsByStatus());
        assertNotNull(analytics.getApplicationTimeline());
    }

    @Test
    void testGetJobAnalyticsWithUnauthorizedAccess() {
        // Given
        Long jobId = 1L;
        User employer = createTestUser(1L);
        User otherEmployer = createTestUser(2L);
        JobPost job = createTestJob(jobId);
        job.setEmployer(otherEmployer);
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enhancedEmployerDashboardService.getJobAnalytics(jobId, employer);
        });
        
        assertEquals("Unauthorized access to job analytics", exception.getMessage());
    }

    @Test
    void testBulkUpdateJobStatuses() {
        // Given
        List<Long> jobIds = Arrays.asList(1L, 2L, 3L);
        String status = "INACTIVE";
        User employer = createTestUser(1L);
        
        JobPost job1 = createTestJob(1L);
        job1.setEmployer(employer);
        JobPost job2 = createTestJob(2L);
        job2.setEmployer(employer);
        JobPost job3 = createTestJob(3L);
        job3.setEmployer(createTestUser(2L)); // Different employer
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(job2));
        when(jobRepository.findById(3L)).thenReturn(Optional.of(job3));

        // When
        int updatedCount = enhancedEmployerDashboardService.bulkUpdateJobStatuses(jobIds, status, employer);

        // Then
        assertEquals(2, updatedCount); // Only 2 jobs belong to the employer
        verify(jobRepository, times(2)).save(any(JobPost.class));
    }

    @Test
    void testGetEnhancedCandidatesForJob() {
        // Given
        Long jobId = 1L;
        User employer = createTestUser(1L);
        JobPost job = createTestJob(jobId);
        job.setEmployer(employer);
        
        User candidate1 = createTestUser(2L);
        candidate1.setRole(UserRole.JOB_SEEKER);
        User candidate2 = createTestUser(3L);
        candidate2.setRole(UserRole.JOB_SEEKER);
        
        JobApplication app1 = createTestApplication(1L, job);
        app1.setUser(candidate1);
        JobApplication app2 = createTestApplication(2L, job);
        app2.setUser(candidate2);
        
        List<JobApplication> applications = Arrays.asList(app1, app2);
        
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobPost(job)).thenReturn(applications);

        // When
        List<EnhancedEmployerDashboardService.EnhancedCandidateInfo> candidates = 
                enhancedEmployerDashboardService.getEnhancedCandidatesForJob(jobId, employer);

        // Then
        assertNotNull(candidates);
        assertEquals(2, candidates.size());
        assertEquals(app1, candidates.get(0).getApplication());
        assertEquals(candidate1, candidates.get(0).getCandidate());
        assertEquals(app2, candidates.get(1).getApplication());
        assertEquals(candidate2, candidates.get(1).getCandidate());
    }

    // Helper methods to create test data
    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(UserRole.EMPLOYER);
        return user;
    }

    private JobPost createTestJob(Long id) {
        JobPost job = new JobPost();
        job.setPostId(id);
        job.setPostProfile("Software Engineer");
        job.setPostedAt(LocalDateTime.now().minusDays(10));
        job.setJobStatus("ACTIVE");
        return job;
    }

    private JobApplication createTestApplication(Long id, JobPost job) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setJobPost(job);
        application.setAppliedAt(LocalDateTime.now().minusDays(5));
        application.setStatus("APPLIED");
        return application;
    }
}