package com.standingcat.event;

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
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
	void enrollUser_endToEnd() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId())
						.with(user("testuser").roles("USER")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.username").value("testuser"));

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));
	}
}
