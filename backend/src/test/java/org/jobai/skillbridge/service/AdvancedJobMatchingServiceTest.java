package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.*;
import org.jobai.skillbridge.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdvancedJobMatchingServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JobRepo jobRepository;
    
    @Mock
    private SkillRepository skillRepository;
    
    @Mock
    private ExperienceRepository experienceRepository;
    
    @Mock
    private EducationRepository educationRepository;
    
    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private AdvancedJobMatchingService advancedJobMatchingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindMatchingCandidates() {
        // Given
        Long jobId = 1L;
        JobPost job = createTestJobPost(jobId);
        List<User> users = Arrays.asList(createTestUser(1L), createTestUser(2L));
        List<Skill> user1Skills = Arrays.asList(createTestSkill(1L, "Java", 8), createTestSkill(2L, "Spring", 7));
        List<Skill> user2Skills = Arrays.asList(createTestSkill(3L, "Python", 9), createTestSkill(4L, "Django", 6));
        List<Experience> user1Experiences = Arrays.asList(createTestExperience(1L, "Developer", LocalDate.now().minusYears(2)));
        List<Experience> user2Experiences = Arrays.asList(createTestExperience(2L, "Engineer", LocalDate.now().minusYears(3)));
        List<Education> user1Educations = Arrays.asList(createTestEducation(1L, "BSc Computer Science"));
        List<Education> user2Educations = Arrays.asList(createTestEducation(2L, "MSc Software Engineering"));

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findAll()).thenReturn(users);
        when(skillRepository.findByUser(any(User.class))).thenReturn(Collections.emptyList());
        when(experienceRepository.findByUser(any(User.class))).thenReturn(Collections.emptyList());
        when(educationRepository.findByUser(any(User.class))).thenReturn(Collections.emptyList());
        
        // Setup specific user data
        when(skillRepository.findByUser(users.get(0))).thenReturn(user1Skills);
        when(skillRepository.findByUser(users.get(1))).thenReturn(user2Skills);
        when(experienceRepository.findByUser(users.get(0))).thenReturn(user1Experiences);
        when(experienceRepository.findByUser(users.get(1))).thenReturn(user2Experiences);
        when(educationRepository.findByUser(users.get(0))).thenReturn(user1Educations);
        when(educationRepository.findByUser(users.get(1))).thenReturn(user2Educations);

        // When
        List<AdvancedJobMatchingService.AdvancedCandidateMatch> matches = 
                advancedJobMatchingService.findMatchingCandidates(jobId, 10);

        // Then
        assertNotNull(matches);
        assertEquals(2, matches.size());
        assertTrue(matches.get(0).getScore().getTotalScore() >= 0.0);
        assertTrue(matches.get(0).getScore().getTotalScore() <= 1.0);
    }

    @Test
    void testFindMatchingJobs() {
        // Given
        Long userId = 1L;
        User user = createTestUser(userId);
        List<JobPost> jobs = Arrays.asList(createTestJobPost(1L), createTestJobPost(2L));
        List<Skill> userSkills = Arrays.asList(createTestSkill(1L, "Java", 8), createTestSkill(2L, "Spring", 7));
        List<Experience> userExperiences = Arrays.asList(createTestExperience(1L, "Developer", LocalDate.now().minusYears(2)));
        List<Education> userEducations = Arrays.asList(createTestEducation(1L, "BSc Computer Science"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jobRepository.findAll()).thenReturn(jobs);
        when(skillRepository.findByUser(user)).thenReturn(userSkills);
        when(experienceRepository.findByUser(user)).thenReturn(userExperiences);
        when(educationRepository.findByUser(user)).thenReturn(userEducations);

        // When
        List<AdvancedJobMatchingService.AdvancedJobMatch> matches = 
                advancedJobMatchingService.findMatchingJobs(userId, 10);

        // Then
        assertNotNull(matches);
        assertEquals(2, matches.size());
        assertTrue(matches.get(0).getScore().getTotalScore() >= 0.0);
        assertTrue(matches.get(0).getScore().getTotalScore() <= 1.0);
    }

    @Test
    void testFindMatchingCandidatesWithNonExistentJob() {
        // Given
        Long jobId = 999L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            advancedJobMatchingService.findMatchingCandidates(jobId, 10);
        });
        
        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void testFindMatchingJobsWithNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            advancedJobMatchingService.findMatchingJobs(userId, 10);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateLearningModels() {
        // Given
        Long jobId = 1L;
        Long userId = 1L;
        JobPost job = createTestJobPost(jobId);
        User user = createTestUser(userId);
        List<Skill> userSkills = Arrays.asList(createTestSkill(1L, "Java", 8));
        Optional<EmployerProfile> employerProfile = Optional.of(createTestEmployerProfile(1L, "Tech Corp"));

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(skillRepository.findByUser(user)).thenReturn(userSkills);
        when(employerProfileRepository.findByUser(any(User.class))).thenReturn(employerProfile);

        // When & Then
        // Should not throw any exception
        assertDoesNotThrow(() -> {
            advancedJobMatchingService.updateLearningModels(jobId, userId);
        });
    }

    @Test
    void testUpdateLearningModelsWithNonExistentJob() {
        // Given
        Long jobId = 999L;
        Long userId = 1L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            advancedJobMatchingService.updateLearningModels(jobId, userId);
        });
        
        assertEquals("Job not found", exception.getMessage());
    }

    // Helper methods to create test data
    private JobPost createTestJobPost(Long id) {
        JobPost job = new JobPost();
        job.setPostId(id);
        job.setPostProfile("Software Engineer");
        job.setReqExperience(3);
        job.setPostTechStack(Arrays.asList("Java", "Spring"));
        job.setLocation("Dhaka");
        User employer = new User();
        employer.setId(100L); // Set an ID for the employer
        job.setEmployer(employer);
        return job;
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(UserRole.JOB_SEEKER);
        user.setCity("Dhaka");
        return user;
    }

    private Skill createTestSkill(Long id, String name, int proficiency) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setName(name);
        skill.setProficiencyLevel(proficiency);
        return skill;
    }

    private Experience createTestExperience(Long id, String position, LocalDate startDate) {
        Experience experience = new Experience();
        experience.setId(id);
        experience.setPosition(position);
        experience.setStartDate(startDate);
        experience.setEndDate(LocalDate.now());
        return experience;
    }

    private Education createTestEducation(Long id, String degree) {
        Education education = new Education();
        education.setId(id);
        education.setDegree(degree);
        return education;
    }
    
    private EmployerProfile createTestEmployerProfile(Long id, String companyName) {
        EmployerProfile profile = new EmployerProfile();
        profile.setId(id);
        profile.setCompanyName(companyName);
        return profile;
    }
}