package com.standingcat.event.service;

import com.standingcat.event.exception.EventNotFoundException;
import com.standingcat.event.exception.NoRolePermissionException;
import com.standingcat.event.exception.UserNotFoundException;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EventRepository;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserService userService;

    public List<Event> getAllEvents() {
        return eventRepository.findByIsHiddenFalse();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event createEvent(Event event, Long adminId) {
        User adminUser = userService.findById(adminId).orElseThrow(() -> new UserNotFoundException("Admin user not found."));
        if(!adminUser.getRoles().contains("ROLE_ADMIN"))
            throw new NoRolePermissionException("Non-Admins cannot create events.");
        event.setOwner(adminUser);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found."));
        eventRepository.delete(event);
    }

    @Transactional
    public Event updateEvent(Event updatedEvent, Long eventId) {
        //Is hidden won't be changed here. That's intended for specific hide/unhide methods,
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found."));
        event.setEventTime(updatedEvent.getEventTime());
        event.setDescription(updatedEvent.getDescription());
        event.setImageUrl(updatedEvent.getImageUrl());
        event.setTitle(updatedEvent.getTitle());
        return eventRepository.save(event);
    }

    @Transactional
    public Event hideEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found."));
        event.setHidden(true);
        return eventRepository.save(event);
    }

    @Transactional
    public Event unHideEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found."));
        event.setHidden(false);
        return eventRepository.save(event);
    }
}
