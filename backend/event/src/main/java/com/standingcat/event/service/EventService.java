package com.standingcat.event.service;

import com.cloudinary.Cloudinary;
import com.standingcat.event.exception.CloudinaryNoReturnException;
import com.standingcat.event.exception.EventNotFoundException;
import com.standingcat.event.exception.NoRolePermissionException;
import com.standingcat.event.exception.UserNotFoundException;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EventRepository;
import jakarta.annotation.Resource;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserService userService;

    @Resource
    private Cloudinary cloudinary;

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


        if(!adminUser.getRoles().contains("ROLE_ADMIN")) {
            throw new NoRolePermissionException("Non-Admins cannot create events.");
        }
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDescriptionMarkdown(descriptionMarkdown);
        event.setEventTime(eventTime);
        event.setCapacity(capacity);
        event.setOwner(adminUser);

        String uploadedPublicId = null;

        if (image != null && !image.isEmpty()) {
            try {
                String uploadFolder = "user_uploads/" + adminUser.getId();
                Map<String, Object> options = Map.of(
                        "folder", uploadFolder,
                        "use_filename", true,
                        "unique_filename", true
                );
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                  image.getBytes(),
                  options
                );
                uploadedPublicId = (String) uploadResult.get("public_id");
                String imageUrl = (String) uploadResult.get("secure_url");
                if(imageUrl == null) {
                    throw new CloudinaryNoReturnException("Cloudinary did not return URL of uploaded image.");
                }
                event.setImageUrl(imageUrl);
                event.setImagePublicId(uploadedPublicId);
                Event saved = eventRepository.save(event);
                System.out.println(">>> [Service] Event saved with ID: " + saved.getId());
                return saved;
            } catch (IOException e) {
                System.out.println(">>> [Service] Failed to save image: " + e.getMessage());
                if(uploadedPublicId != null) {
                    try{
                        cloudinary.uploader().destroy(uploadedPublicId, Map.of());//
                    } catch (Exception err) {
                        //log could be appropriate here
                        System.err.println("Failed to clean cloudinary upload: " + err.getMessage());
                    }
                }
                //change with better runtime exception
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

    //to do: image upload
    @Transactional
    public Event updateEvent(Long eventId,
                                     String title,
                                     String description,
                                     String descriptionMarkdown,
                                     LocalDateTime eventTime,
                                     Integer capacity,
                                     MultipartFile image,
                                     User adminUser) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found."));
        if (!adminUser.getRoles().contains("ROLE_ADMIN")
                || !event.getOwner().getId().equals(adminUser.getId())) {
            throw new NoRolePermissionException("User cannot update this event.");
        }
        event.setEventTime(eventTime);
        event.setDescription(description);
        event.setDescriptionMarkdown(descriptionMarkdown);
        event.setCapacity(capacity);
        event.setTitle(title);
        //return eventRepository.save(event);
        String previousPublicId = event.getImagePublicId();
        String newPublicId = null;

        try {
            if (image != null && !image.isEmpty()) {
                String userFolder = "user_uploads/" + adminUser.getId();

                Map<String, Object> uploadOptions = Map.of(
                        "folder", userFolder,
                        "use_filename", true,
                        "unique_filename", true
                );

                Map<?, ?> uploadResult = cloudinary.uploader().upload(image.getBytes(), uploadOptions);
                String imageUrl = (String) uploadResult.get("secure_url");
                newPublicId = (String) uploadResult.get("public_id");

                if (imageUrl == null) {
                    throw new RuntimeException("Cloudinary upload did not return a URL.");
                }

                event.setImageUrl(imageUrl);
                event.setImagePublicId(newPublicId);
            }

            Event updatedEvent = eventRepository.save(event);

            if (newPublicId != null && previousPublicId != null && !previousPublicId.isBlank()) {
                try {
                    cloudinary.uploader().destroy(previousPublicId, Map.of());
                } catch (Exception cleanupError) {
                    System.err.println("WARN: Failed to delete old Cloudinary image for event: "
                            + cleanupError.getMessage());
                }
            }

            return updatedEvent;

        } catch (Exception e) {
            //upload success but failure occurs after, need to clean up upload
            if (newPublicId != null) {
                try {
                    cloudinary.uploader().destroy(newPublicId, Map.of());
                } catch (Exception cleanupError) {
                    System.err.println("WARNING: Failed to clean up new Cloudinary image after update failure: "
                            + cleanupError.getMessage());
                }
            }
            //change to better error
            throw new RuntimeException("Event update failed: " + e.getMessage(), e);
        }
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
