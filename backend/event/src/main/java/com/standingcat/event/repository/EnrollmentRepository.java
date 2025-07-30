package com.standingcat.event.repository;

import com.standingcat.event.model.Enrollment;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUser(User user); //user sees all his enrollments
    List<Enrollment> findByEvent(Event event); //admin can see all users enrolled to event
    Optional<Enrollment> findByUserAndEvent(User user, Event event); //check if already enrolled

}
