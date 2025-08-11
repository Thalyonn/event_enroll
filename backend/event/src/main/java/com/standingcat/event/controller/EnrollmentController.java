package com.standingcat.event.controller;

import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    @Autowired
    UserService userService;
    @Autowired
    EnrollmentService enrollmentService;
    //enroll user

    @PostMapping("/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> enrollUserToEvent(@PathVariable Long eventId, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> enrollingUser = userService.findByUsername(userDetails.getUsername());
        if (enrollingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authenticated user not found."));
        }
        Long userId = enrollingUser.get().getId();

        try {
            Enrollment enrollment = enrollmentService.enrollUserToEvent(userId, eventId);
            return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    //admins view users enrolled to an event
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEnrollmentsForEvent(@PathVariable Long eventId) {
        try {
            List<Enrollment> enrollmentList = enrollmentService.getEnrollmentsForEvent(eventId);
            return ResponseEntity.ok(enrollmentList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    //user see events they are enrolled in

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyEnrollments(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authenticated user not found."));
        }
        Long userId = currentUser.get().getId();

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsForUser(userId);
        return ResponseEntity.ok(enrollments);
    }

    //users can un-enroll themselves
    @DeleteMapping("/{eventId}") 
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unEnrollUserFromEvent(@PathVariable Long eventId, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authenticated user not found."));
        }
        Long userId = currentUser.get().getId();

        try {
            enrollmentService.unEnrollUserFromEvent(userId, eventId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}
