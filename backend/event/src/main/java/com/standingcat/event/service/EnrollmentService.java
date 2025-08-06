package com.standingcat.event.service;

import com.standingcat.event.exception.EventNotFoundException;
import com.standingcat.event.exception.UserNotFoundException;
import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setEvent(event);
        enrollment.setEnrollmentTime(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        emailService.sendEnrollmentConfirmation(user.getEmail(), event.getTitle());

        return savedEnrollment;


    }



}
