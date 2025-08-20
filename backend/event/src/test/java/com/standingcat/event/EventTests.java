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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
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

    /*updateEvent (admin only)
    Success - Updates event details > returns 200 OK.
    Fail - Event not found > 404 Not Found.
    Fail - Non-admin tries to update > 403 Forbidden.*/

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void updateEvent_success() throws Exception {
/*
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event event) {
            try {
                Event updatedEvent = eventService.updateEvent(event, id);
                return ResponseEntity.ok(updatedEvent);
            } catch(RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
        }*/
        String eventJson = """
        {
            "title": "Updated Event",
            "description": "Updated Description",
            "imageUrl": "updatedimage.jpg",
            "eventTime": "2031-01-01T10:00:00",
            "capacity": 51
        }
        """;
        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                .contentType("application/json")
                .content(eventJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.imageUrl").value("updatedimage.jpg"))
                .andExpect(jsonPath("$.capacity").value(51));

        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assertEquals("Updated Event", updatedEvent.getTitle());
        assertEquals("Updated Description", updatedEvent.getDescription());
        assertEquals("updatedimage.jpg", updatedEvent.getImageUrl());
        assertEquals(51, updatedEvent.getCapacity());
        assertEquals(LocalDateTime.of(2031, 1, 1, 10, 0), updatedEvent.getEventTime());



    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void updateEvent_notFound() throws Exception {
        String eventJson = """
        {
            "title": "Updated Event",
            "description": "Updated Description",
            "imageUrl": "updatedimage.jpg",
            "eventTime": "2031-01-01T10:00:00",
            "capacity": 51
        }
        """;
        mockMvc.perform(put("/api/events/{id}", testEvent.getId()+9999)
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event not found."));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateEvent_notAdmins() throws Exception {
        String eventJson = """
        {
            "title": "Updated Event",
            "description": "Updated Description",
            "imageUrl": "updatedimage.jpg",
            "eventTime": "2031-01-01T10:00:00",
            "capacity": 51
        }
        """;
        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .contentType("application/json")
                        .content(eventJson))
                .andExpect(status().isForbidden());
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assertEquals(testEvent.getTitle(), updatedEvent.getTitle());
        assertEquals(testEvent.getDescription(), updatedEvent.getDescription());
        assertEquals(testEvent.getImageUrl(), updatedEvent.getImageUrl());
        assertEquals(testEvent.getCapacity(), updatedEvent.getCapacity());
        assertEquals(testEvent.getEventTime(), updatedEvent.getEventTime());

    }
    /*deleteEvent (admin only)
    Success - Deletes event > returns 204 No Content.
    Fail - Event not found > 404 Not Found.
    Fail - Non-admin tries to delete > 403 Forbidden.*/
    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void deleteEvent_success() throws Exception {
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

        mockMvc.perform(delete("/api/events/{id}", createdEvent.getId()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertFalse(eventRepository.findById(createdEvent.getId()).isPresent());

        User refreshedAdmin = userRepository.findByUsername("testadmin").orElseThrow();
        assertFalse(refreshedAdmin.getCreatedEvents().contains(createdEvent));

    }

    @Test
    @WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void deleteEvent_eventNotFound() throws Exception {
        User testAdmin = userRepository.save(new User(
                null,
                "testadmin",
                "password",
                "admin@example.com",
                Set.of("ROLE_ADMIN"),
                new HashSet<>(),
                new HashSet<>()
        ));


        mockMvc.perform(delete("/api/events/{id}", testEvent.getId()+9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event not found."));



    }

    @Test
    @WithMockUser(username = "testuser", roles = {"user"})
    void deleteEvent_notAdmin() throws Exception {

        mockMvc.perform(delete("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isForbidden());
        assertTrue(eventRepository.findById(testEvent.getId()).isPresent());

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void hideEvent_success() throws Exception {
        mockMvc.perform(patch("/api/events/{id}/hide", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hidden").value(true));

        entityManager.flush();
        entityManager.clear();

        Event updated = eventRepository.findById(testEvent.getId()).orElseThrow();
        assertTrue(updated.isHidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void hideEvent_notFound() throws Exception {
        mockMvc.perform(patch("/api/events/{id}/hide", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event not found."));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void hideEvent_forbidden() throws Exception {
        mockMvc.perform(patch("/api/events/{id}/hide", testEvent.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void unHideEvent_success() throws Exception {
        User testAdmin = userRepository.save(new User(
                null,
                "testadmin",
                "password",
                "admin@example.com",
                Set.of("ROLE_ADMIN"),
                new HashSet<>(),
                new HashSet<>()
        ));
        Event hiddenEvent = eventRepository.save(new Event(
                null,
                "Test Event",
                "Description",
                "image.jpg",
                LocalDateTime.now(),
                true,
                testAdmin,
                5,
                new HashSet<>()));

        mockMvc.perform(patch("/api/events/{id}/unhide", hiddenEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hidden").value(false));

        entityManager.flush();
        entityManager.clear();

        Event updated = eventRepository.findById(hiddenEvent.getId()).orElseThrow();
        assertFalse(updated.isHidden());
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void unHideEvent_notFound() throws Exception {

        mockMvc.perform(patch("/api/events/{id}/unhide", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event not found."));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void unHideEvent_forbidden() throws Exception {
        Event hiddenEvent = eventRepository.save(new Event(
                null,
                "Test Event",
                "Description",
                "image.jpg",
                LocalDateTime.now(),
                true,
                testUser,
                5,
                new HashSet<>()));

        mockMvc.perform(patch("/api/events/{id}/unhide", testEvent.getId()))
                .andExpect(status().isForbidden());
    }




}
