package com.standingcat.event;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventTests {
    

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
                new HashSet<>(),
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

    //getAllEvents
    //Success - Returns list of all successful events
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAllEvents_onlyVisibleEventsReturned() throws Exception {
        Event visibleEvent = new Event();
        visibleEvent.setTitle("Visible Event");
        visibleEvent.setDescription("Visible desc");
        visibleEvent.setImageUrl("image.jpg");
        visibleEvent.setEventTime(LocalDateTime.now().plusDays(1));
        visibleEvent.setCapacity(10);
        visibleEvent.setHidden(false);
        visibleEvent.setOwner(testUser);
        eventRepository.save(visibleEvent);

        Event hiddenEvent = new Event();
        hiddenEvent.setTitle("Hidden Event");
        hiddenEvent.setDescription("Hidden desc");
        hiddenEvent.setImageUrl("image.jpg");
        hiddenEvent.setEventTime(LocalDateTime.now().plusDays(2));
        hiddenEvent.setCapacity(5);
        hiddenEvent.setHidden(true);
        hiddenEvent.setOwner(testUser);
        eventRepository.save(hiddenEvent);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.hasItem("Visible Event")))
                .andExpect(jsonPath("$[*].hidden").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))));
    }
    //if no events, should return empty
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAllEvents_noEvents_returnsEmptyList() throws Exception {
        //clear repository to simulate no events
        eventRepository.deleteAll();
        entityManager.flush();

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //Success - get event by id
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getEventById_success() throws Exception {
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.hidden").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getEventById_notFound() throws Exception {
        mockMvc.perform(get("/api/events/{id}", 999L)) //non-existent event
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void createEvent_success() throws Exception {
        User testAdmin = userRepository.save(new User(
                null,
                "testadmin",
                "password",
                "admin@example.com",
                Set.of("ROLE_ADMIN"),
                new HashSet<>(),
                new HashSet<>()
        ));

        String eventJson = """
        {
            "title": "Admin Event",
            "description": "Created by admin",
            "imageUrl": "image.jpg",
            "eventTime": "2030-01-01T10:00:00",
            "capacity": 50
        }
        """;

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Admin Event"))
                .andExpect(jsonPath("$.owner.username").value("testadmin"));

        entityManager.flush();
        entityManager.clear();

        User persistedAdmin = userRepository.findByUsername("testadmin").orElseThrow();
        Event createdEvent = eventRepository.findAll()
                .stream()
                .filter(e -> e.getTitle().equals("Admin Event"))
                .findFirst()
                .orElseThrow();

        assertEquals("testadmin", createdEvent.getOwner().getUsername());
        assertTrue(persistedAdmin.getCreatedEvents().contains(createdEvent));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void createEvent_authAdminNotFound() throws Exception {


        String eventJson = """
        {
            "title": "Admin Event",
            "description": "Created by admin",
            "imageUrl": "image.jpg",
            "eventTime": "2030-01-01T10:00:00",
            "capacity": 50
        }
        """;

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authenticated admin user not found."));

    }

    //Fail - Invalid event data (missing title, invalid date) -> 400 Bad Request.
    //might need to re-verify this or separate to different tests
    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void createEvent_invalidEventData() throws Exception {
        User testAdmin = userRepository.save(new User(
                null,
                "testadmin",
                "password",
                "admin@example.com",
                Set.of("ROLE_ADMIN"),
                new HashSet<>(),
                new HashSet<>()
        ));

        String eventJson = """
        {
            "description": "Created by admin",
            "imageUrl": "image.jpg",
            "eventTime": "thisisnotarealdatetime",
            "capacity": 50
        }
        """;

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isBadRequest());

    }
    //Fail — Non-admin tries to create > 403 Forbidden.
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createEvent_nonAdmin() throws Exception {
        String eventJson = """
        {
            "title": "Admin Event",
            "description": "Created by admin",
            "imageUrl": "image.jpg",
            "eventTime": "2030-01-01T10:00:00",
            "capacity": 50
        }
        """;

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isForbidden());

        assertFalse(eventRepository.existsByTitle("Admin Event"),
                "Event should not exist in the database");

        User user = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(user.getCreatedEvents().isEmpty(),
                "User should not have created any events");

    }
}
