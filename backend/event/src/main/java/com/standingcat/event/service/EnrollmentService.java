package com.standingcat.event.service;

import com.standingcat.event.exception.*;
import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Enrollment enrollUserToEvent(Long userId, Long eventId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));
        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not Found"));

        //null means unlimited capacity
        if(event.getCapacity() != null && event.getEnrollments().size() >= event.getCapacity()) {
            throw new InsufficientCapacityException("Event is at full capacity.");
        }

        if(enrollmentRepository.findByUserAndEvent(user, event).isPresent()) {
            throw new UserAlreadyEnrolledException("User already enrolled.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setEvent(event);
        enrollment.setEnrollmentTime(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        emailService.sendEnrollmentConfirmation(user.getEmail(), event.getTitle());

        return savedEnrollment;


    }

    @Transactional
    public void unEnrollUserFromEvent(Long userId, Long eventId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));
        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not Found"));
        Enrollment enrollment = enrollmentRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not Found"));
        enrollmentRepository.delete(enrollment);
    }

    public List<Enrollment> getEnrollmentsForEvent(Long eventId) {
        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found."));
        return enrollmentRepository.findByEvent(event);
    }

    public List<Enrollment> getEnrollmentsForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return enrollmentRepository.findByUser(user);
    }



}
