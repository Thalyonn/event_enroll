package com.standingcat.event.repository;

import com.standingcat.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIsHiddenFalse(); //return all events where is hidden is false
    List<Event> findByOwnerId(Long ownerId); //find all events owned by a user

    boolean existsByTitle(String title);
}
