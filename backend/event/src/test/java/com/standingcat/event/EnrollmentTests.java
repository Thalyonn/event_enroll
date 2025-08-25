package com.standingcat.event;

import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EnrollmentRepository;
import com.standingcat.event.repository.EventRepository;
import com.standingcat.event.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class EnrollmentTests {


	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	@Autowired
	private EntityManager entityManager;

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
				new HashSet<>()));
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

	//Created to see if enrollments are added to both user and event too
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void enrollUser_successPersistence() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.username").value("testuser"));

		//Check enrollment exists in repository
		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));

		//Reload entities to ensure persistence context is updated
		User persistedUser = userRepository.findById(testUser.getId())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Event persistedEvent = eventRepository.findById(testEvent.getId())
				.orElseThrow(() -> new RuntimeException("Event not found"));

		assertEquals(1, persistedUser.getEnrollments().size());
		assertEquals(1, persistedEvent.getEnrollments().size());

		Enrollment enrollmentFromUser = persistedUser.getEnrollments().iterator().next();
		Enrollment enrollmentFromEvent = persistedEvent.getEnrollments().iterator().next();

		assertEquals(testUser.getId(), enrollmentFromUser.getUser().getId());
		assertEquals(testEvent.getId(), enrollmentFromEvent.getEvent().getId());
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
		Enrollment enrollment = new Enrollment();
		enrollment.setUser(testUser);
		enrollment.setEvent(testEvent);
		enrollment.setEnrollmentTime(LocalDateTime.now());
		enrollmentRepository.save(enrollment);

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));

		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("User already enrolled."));
	}

	//enroll while event is at full capacity
	@Test
	void enrollUser_eventFull() throws Exception {
		// Arrange — create event with capacity 2
		Event fullEvent = eventRepository.save(new Event(
				null, "Full Event", "Description", "image.jpg",
				LocalDateTime.now(), false, testUser, 2, new HashSet<>()
		));

		// Create users in the DB
		User firstUser = userRepository.save(new User(
				null, "firstuser", "password", "first@example.com",
				Set.of("ROLE_USER"), null, new HashSet<>()
		));
		User secondUser = userRepository.save(new User(
				null, "seconduser", "password", "second@example.com",
				Set.of("ROLE_USER"), null, new HashSet<>()
		));
		User thirdUser = userRepository.save(new User(
				null, "thirduser", "password", "third@example.com",
				Set.of("ROLE_USER"), null, new HashSet<>()
		));

		// Enroll first user
		mockMvc.perform(post("/api/enrollments/{eventId}", fullEvent.getId())
						.with(user(firstUser.getUsername()).roles("USER")))
				.andExpect(status().isCreated());

		// Enroll second user
		mockMvc.perform(post("/api/enrollments/{eventId}", fullEvent.getId())
						.with(user(secondUser.getUsername()).roles("USER")))
				.andExpect(status().isCreated());

		// Attempt to enroll third user — should fail because capacity is full
		mockMvc.perform(post("/api/enrollments/{eventId}", fullEvent.getId())
						.with(user(thirdUser.getUsername()).roles("USER")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Event is at full capacity."));
	}

	@Test
	@WithMockUser(username = "testuser", roles = "USER")
	void enrollUserShouldNotReturnPasswordField() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				// Ensure "password" is not included anywhere in the JSON
				.andExpect(jsonPath("$.user.password").doesNotExist())
				.andExpect(jsonPath("$.event.owner.password").doesNotExist());
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

	//event does not exist
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void getEnrollmentsForEvent_eventNotFound() throws Exception {
		Long ghostEventId = testEvent.getId() + 9999;
		assertFalse(eventRepository.existsById(ghostEventId));
		mockMvc.perform(get("/api/enrollments/event/{eventId}", ghostEventId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Event not found."));

	}

	//non admin tries to get all enrollments
	@Test
	@WithMockUser(username = "testuser", roles = "USER")
	void getEnrollmentsForEvent_notAdmin() throws Exception {
		mockMvc.perform(get("/api/enrollments/event/{eventId}", testEvent.getId()))
				.andExpect(status().isForbidden()); //403

	}


	//get my enrollments
	@Test
	@WithMockUser(username = "testuser", roles = "USER")
	void getMyEnrollments_success() throws Exception {
		Enrollment enrollment1 = new Enrollment();
		enrollment1.setUser(testUser);
		enrollment1.setEvent(testEvent);
		enrollment1.setEnrollmentTime(LocalDateTime.now());
		enrollmentRepository.save(enrollment1);

		User otherUser = userRepository.save(new User(
				null,
				"otheruser",
				"password",
				"other@example.com",
				Set.of("ROLE_USER"),
				null,
				null
		));
		Event otherEvent = eventRepository.save(new Event(
				null,
				"Other Event",
				"Description",
				"image.jpg",
				LocalDateTime.now(),
				false,
				otherUser,
				5,
				new HashSet<>()
		));
		Enrollment enrollment2 = new Enrollment();
		enrollment2.setUser(otherUser);
		enrollment2.setEvent(otherEvent);
		enrollment2.setEnrollmentTime(LocalDateTime.now());
		enrollmentRepository.save(enrollment2);

		Event anotherEvent = eventRepository.save(new Event(
				null,
				"Another Event",
				"Description",
				"image.jpg",
				LocalDateTime.now(),
				false,
				testUser,
				5,
				new HashSet<>()
		));
		Enrollment enrollment3 = new Enrollment();
		enrollment3.setUser(testUser);
		enrollment3.setEvent(anotherEvent);
		enrollment3.setEnrollmentTime(LocalDateTime.now());
		enrollmentRepository.save(enrollment3);

		mockMvc.perform(get("/api/enrollments/my-enrollments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2)) // should return only the 2 testUser enrollments
				.andExpect(jsonPath("$[0].user.username").value("testuser"))
				.andExpect(jsonPath("$[1].user.username").value("testuser"));
	}

	//user is authenticated but not in the database while trying to get enrollments
	@Test
	@WithMockUser(username = "fakeuser", roles = {"USER"})
	void getMyEnrollments_userNotFound() throws	Exception {
		mockMvc.perform(get("/api/enrollments/my-enrollments"))
				.andExpect(status().isUnauthorized()) // 401
				.andExpect(jsonPath("$.error").value("Authenticated user not found."));
	}

	//no enrollments
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void getMyEnrollments_noEnrollments() throws Exception {
		mockMvc.perform(get("/api/enrollments/my-enrollments"))
				.andExpect(status().isOk()) // Expect a 200 OK status
				.andExpect(jsonPath("$").isArray()) // Verify the root element is a JSON array
				.andExpect(jsonPath("$").isEmpty());
	}

	//un-enroll user from event
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void unEnroll_success() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.username").value("testuser"));

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));
		mockMvc.perform(delete("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isNoContent());

		assertFalse(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void unEnroll_successPersistence() throws Exception {
		mockMvc.perform(post("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isCreated());

		assertTrue(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));

		mockMvc.perform(delete("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		//Reload fresh entities from DB
		User refreshedUser = userRepository.findById(testUser.getId())
				.orElseThrow();
		Event refreshedEvent = eventRepository.findById(testEvent.getId())
				.orElseThrow();

		assertTrue(refreshedUser.getEnrollments().isEmpty(),
				"User should not have enrollments anymore");
		assertTrue(refreshedEvent.getEnrollments().isEmpty(),
				"Event should not have enrollments anymore");
		assertFalse(enrollmentRepository.existsByUserAndEvent(testUser, testEvent),
				"Enrollment row should be deleted");
	}




	//Fail — Authenticated user not found in DB - returns 401 Unauthorized.
	@Test
	@WithMockUser(username = "fakeuser", roles = {"USER"})
	void unEnroll_userNotFound() throws	Exception {
		mockMvc.perform(delete("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Authenticated user not found."));
	}
	//Fail — Not enrolled in event - returns 400 Bad Request with error message.
	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void unEnroll_userNotEnrolled() throws	Exception {
		assertFalse(enrollmentRepository.existsByUserAndEvent(testUser, testEvent));
		mockMvc.perform(delete("/api/enrollments/{eventId}", testEvent.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Enrollment not found."));
	}


}

