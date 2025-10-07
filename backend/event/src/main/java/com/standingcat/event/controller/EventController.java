package com.standingcat.event.controller;

import com.standingcat.event.dto.EventResponse;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.service.EventService;
import com.standingcat.event.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    EventService eventService;
    @Autowired
    private UserService userService;
//    @GetMapping
//    public ResponseEntity<List<Event>> getAllEvents() {
//        List<Event> events = eventService.getAllEvents();
//        return ResponseEntity.ok(events);
//    }

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventResponse> response = events.stream()
                .map(EventResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}") //Anyone can view a single event
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("eventTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eventTime,
                                         @RequestParam("capacity") Integer capacity,
                                         @RequestParam(value = "image", required = false) MultipartFile image,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println(">>> [Controller] Incoming createEvent request");
        System.out.println(">>> [Controller] UserDetails username: " + userDetails.getUsername());
        System.out.println(">>> [Controller] UserDetails authorities: " + userDetails.getAuthorities());
        System.out.println(">>> [Controller] Title: " + title + ", Desc: " + description + ", EventTime: " + eventTime + ", Capacity: " + capacity);
        if (image != null) {
            System.out.println(">>> [Controller] Image received: " + image.getOriginalFilename());
        } else {
            System.out.println(">>> [Controller] No image provided");
        }

        Optional<User> adminUser = userService.findByUsername(userDetails.getUsername());
        if(adminUser.isEmpty()) {
            System.out.println(">>> [Controller] No matching user found in DB");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated admin user not found."));

        }
        try {
            Event createdEvent = eventService.createEvent(
                    title, description, eventTime, capacity, image, adminUser.get()
            );
            System.out.println(">>> [Controller] Event created successfully: " + createdEvent.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
        } catch (RuntimeException e) {
            System.out.println(">>> [Controller] Error during event creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        try {
            Event updatedEvent = eventService.updateEvent(event, id);
            return ResponseEntity.ok(updatedEvent);
        } catch(RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/hide") // Only ADMINs can hide events
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hideEvent(@PathVariable Long id) {
        try {
            Event hiddenEvent = eventService.hideEvent(id);
            return ResponseEntity.ok(hiddenEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/unhide") // Only ADMINs can unhide events
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unHideEvent(@PathVariable Long id) {
        try {
            Event unhiddenEvent = eventService.unHideEvent(id);
            return ResponseEntity.ok(unhiddenEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
