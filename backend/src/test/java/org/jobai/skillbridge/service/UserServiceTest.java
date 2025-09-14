package org.jobai.skillbridge.service;

import org.jobai.skillbridge.model.User;
import org.jobai.skillbridge.model.UserRole;
import org.jobai.skillbridge.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.JOB_SEEKER);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User savedUser = userService.saveUser(user);

        // Then
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserById() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.JOB_SEEKER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<User> foundUser = userService.getUserById(1L);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }
}