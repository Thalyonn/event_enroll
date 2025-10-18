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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Event createEvent(
            String title,
            String description,
            String descriptionMarkdown,
            LocalDateTime eventTime,
            Integer capacity,
            MultipartFile image,
            User adminUser) {
        System.out.println(">>> [Service] Validating roles for user: " + adminUser.getUsername());
        System.out.println(">>> [Service] DB roles: " + adminUser.getRoles());

        if(!adminUser.getRoles().contains("ROLE_ADMIN")) {
            System.out.println(">>> [Service] User does NOT have ROLE_ADMIN. Throwing exception.");
            throw new NoRolePermissionException("Non-Admins cannot create events.");
        }
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDescriptionMarkdown(descriptionMarkdown);
        event.setEventTime(eventTime);
        event.setCapacity(capacity);
        event.setOwner(adminUser);

        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, filename);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                event.setImageUrl("/uploads/" + filename);
                System.out.println(">>> [Service] Image saved to: " + filePath);
            } catch (IOException e) {
                System.out.println(">>> [Service] Failed to save image: " + e.getMessage());
                throw new RuntimeException("Failed to save image", e);
            }
        }
        else {
            System.out.println(">>> [Service] No image provided, skipping upload.");
        }
        Event saved = eventRepository.save(event);
        System.out.println(">>> [Service] Event saved with ID: " + saved.getId());
        return saved;
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
        event.setDescriptionMarkdown(updatedEvent.getDescriptionMarkdown());
        event.setImageUrl(updatedEvent.getImageUrl());
        event.setCapacity(updatedEvent.getCapacity());
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
