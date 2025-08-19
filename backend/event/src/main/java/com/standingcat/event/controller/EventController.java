package com.standingcat.event.controller;

import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.service.EventService;
import com.standingcat.event.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}") //Anyone can view a single event
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestBody Event event, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> adminUser = userService.findByUsername(userDetails.getUsername());
        if(adminUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authenticated admin user not found."));

        }
        Long adminId = adminUser.get().getId();

        try {
            Event createdEvent = eventService.createEvent(event, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
        } catch (RuntimeException e) {
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
