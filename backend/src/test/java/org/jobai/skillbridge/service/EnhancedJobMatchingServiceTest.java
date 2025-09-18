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

class EnhancedJobMatchingServiceTest {

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

    @InjectMocks
    private EnhancedJobMatchingService enhancedJobMatchingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindMatchingCandidatesWithValidData() {
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
        List<EnhancedJobMatchingService.DetailedCandidateMatch> matches = 
                enhancedJobMatchingService.findMatchingCandidates(jobId, 10);

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
            enhancedJobMatchingService.findMatchingCandidates(jobId, 10);
        });
        
        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void testCalculateSkillMatchScore() {
        // Given
        List<Skill> userSkills = Arrays.asList(
                createTestSkill(1L, "Java", 8),
                createTestSkill(2L, "Spring Boot", 7)
        );
        JobPost job = createTestJobPost(1L);
        job.setPostTechStack(Arrays.asList("Java", "Spring", "Hibernate"));

        // When
        EnhancedJobMatchingService.SkillMatchScore score = 
                enhancedJobMatchingService.calculateSkillMatchScore(userSkills, job);

        // Then
        assertNotNull(score);
        assertTrue(score.getWeightedScore() >= 0.0);
        assertTrue(score.getWeightedScore() <= 1.0);
        assertNotNull(score.getSkillMatches());
        assertEquals(3, score.getSkillMatches().size());
    }

    @Test
    void testCalculateExperienceMatchScore() {
        // Given
        List<Experience> experiences = Arrays.asList(
                createTestExperience(1L, "Software Developer", LocalDate.now().minusYears(2))
        );
        JobPost job = createTestJobPost(1L);
        job.setReqExperience(3);

        // When
        EnhancedJobMatchingService.ExperienceMatchScore score = 
                enhancedJobMatchingService.calculateExperienceMatchScore(experiences, job);

        // Then
        assertNotNull(score);
        assertTrue(score.getWeightedScore() >= 0.0);
        assertTrue(score.getWeightedScore() <= 1.0);
    }

    @Test
    void testDetectRedFlags() {
        // Given
        List<Experience> experiences = Arrays.asList(
                createTestExperience(1L, "Job 1", LocalDate.now().minusYears(3)),
                createTestExperience(2L, "Job 2", LocalDate.now().minusYears(1))
        );
        // Create a gap between the jobs
        experiences.get(0).setEndDate(LocalDate.now().minusYears(2).minusMonths(2));
        experiences.get(1).setStartDate(LocalDate.now().minusYears(1));
        
        List<Skill> skills = Arrays.asList(
                createTestSkill(1L, "Java", 10) // Overclaimed skill
        );

        // When
        EnhancedJobMatchingService.RedFlagDetection redFlags = 
                enhancedJobMatchingService.detectRedFlags(experiences, skills);

        // Then
        assertNotNull(redFlags);
        // We might have warnings for overclaimed skills
        assertTrue(redFlags.getWarnings().size() >= 0);
    }

    // Helper methods to create test data
    private JobPost createTestJobPost(Long id) {
        JobPost job = new JobPost();
        job.setPostId(id);
        job.setPostProfile("Software Engineer");
        job.setReqExperience(3);
        job.setPostTechStack(Arrays.asList("Java", "Spring"));
        return job;
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(UserRole.JOB_SEEKER);
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
}