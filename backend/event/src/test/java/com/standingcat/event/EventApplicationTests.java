package com.standingcat.event;

import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EnrollmentRepository;
import com.standingcat.event.repository.EventRepository;
import com.standingcat.event.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	private User testUser;
	private Event testEvent;

	@BeforeEach
	void setup() {
		testUser = userRepository.save(new User(
				null,
				"testuser",
				"password",
				"test@example.com",
				Set.of("ROLE_USER"),
				null,
				null));
		testEvent = eventRepository.save(new Event(
				null,
				"Test Event",
				"Description",
				"image.jpg",
				LocalDateTime.now(),
				false,
				testUser,
				5,
				new HashSet<>()));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void enrollUser_success() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.username").value("testuser"));

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));
	}

	@Test
	@WithMockUser(username = "fakeuser", roles = {"USER"})
	void enrollUser_userNotFound() throws Exception {
		//event exists, user doesn't

		// Act + Assert
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isUnauthorized()) // 401
				.andExpect(jsonPath("$.error").value("Authenticated user not found."));
	}

	//already enrolled in event
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void enrollUser_userAlreadyEnrolled() throws Exception {
		// Arrange — manually insert enrollment so user is already enrolled
		Enrollment enrollment = new Enrollment();
		enrollment.setUser(testUser);
		enrollment.setEvent(testEvent);
		enrollment.setEnrollmentTime(LocalDateTime.now());
		enrollmentRepository.save(enrollment);

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));

		// Act + Assert — trying to enroll again should fail
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("User already enrolled."));
	}

	//event doesn't exist
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void enrollUser_eventNotFound() throws Exception {
		//have to make sure ghostEventId isnt the same as testEvent id so we add a big number
		Long ghostEventId = testEvent.getId() + 9999;
		assertFalse(eventRepository.existsById(ghostEventId));
		mockMvc.perform(post("/api/enrollments/{eventId}", ghostEventId))
				.andExpect(status().isBadRequest()) // 401
				.andExpect(jsonPath("$.error").value("Event not found."));
	}

	//getEnrollmentsForEvent
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void getEnrollmentsForEvent_success() throws Exception {
		mockMvc.perform(get("/api/enrollments/event/{eventId}", testEvent.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

}
