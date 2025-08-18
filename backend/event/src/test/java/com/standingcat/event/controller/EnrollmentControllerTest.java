package com.standingcat.event.controller;

import com.standingcat.event.config.SecurityConfig;
import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.UserRepository;
import com.standingcat.event.service.EmailService;
import com.standingcat.event.service.EnrollmentService;
import com.standingcat.event.service.EventService;
import com.standingcat.event.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class)
public class EnrollmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // Add this MockitoBean to provide the UserRepository dependency.
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EmailService emailService;

    private User testUser;
    private Event testEvent;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        // Set up common objects for our tests.
        testUser = new User(1L, "testuser", "password", "test@example.com", Set.of("ROLE_USER"), null, null);
        testEvent = new Event(200L, "Test Event", "Description", "image.jpg", LocalDateTime.now(), false, testUser, 5, new HashSet<>());
        testEnrollment = new Enrollment(300L, LocalDateTime.now(), testUser, testEvent);

        // Mock the findByUsername method for Spring Security to work.
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("POST /api/enrollments/{eventId} should enroll user in an event successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void enrollUser_shouldReturnOkAndEnrollment() throws Exception {
        when(enrollmentService.enrollUserToEvent(eq(testUser.getId()), eq(testEvent.getId()))).thenReturn(testEnrollment);

        mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
                .andExpect(status().isCreated()) // 3. Assert - Check the response status
                .andExpect(jsonPath("$.id").value(300L)) // Assert the JSON content
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(enrollmentService, times(1)).enrollUserToEvent(eq(testUser.getId()), eq(testEvent.getId()));
    }
    /*
    @Test
    @DisplayName("DELETE /api/enrollments/{eventId} should unenroll user successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void unEnrollUser_shouldReturnOk() throws Exception {
        doNothing().when(enrollmentService).unEnrollUserFromEvent(eq(testUser.getId()), eq(testEvent.getId()));

        mockMvc.perform(delete("/api/enrollments/{eventId}", testEvent.getId()))
                .andExpect(status().isNoContent()); // 3. Assert - Check for success status

        verify(enrollmentService, times(1)).unEnrollUserFromEvent(eq(testUser.getId()), eq(testEvent.getId()));
    }
    */

}
