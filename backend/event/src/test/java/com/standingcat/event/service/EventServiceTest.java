package com.standingcat.event.service;

import com.standingcat.event.exception.EventNotFoundException;
import com.standingcat.event.exception.NoRolePermissionException;
import com.standingcat.event.exception.UserNotFoundException;
import com.standingcat.event.model.Event;
import com.standingcat.event.model.User;
import com.standingcat.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;



@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Test
    void createEvent_sets_owner_saves_admin() {
        User admin = new User();
        admin.setId(10L);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);
        Event input = new Event();
        input.setTitle("Fighting Game Tournament");

        when(userService.findById(10L)).thenReturn(Optional.of(admin));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event saved = eventService.createEvent(input, 10L);

        // owner should be set to admin
        assertSame(admin, saved.getOwner());
        assertEquals("Fighting Game Tournament", saved.getTitle());
        verify(userService).findById(10L);
        verify(eventRepository).save(eventCaptor.capture());

        Event captured = eventCaptor.getValue();
        assertSame(admin, captured.getOwner());
    }

    @Test
    void createEvent_user_not_found() {
        when(userService.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> eventService.createEvent(new Event(), 1L));

        assertTrue(ex.getMessage().contains("Admin user not found"));
        verify(userService, times(1)).findById(1L);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void createEvent_user_not_admin() {
        User user = new User();
        user.setId(1L);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        NoRolePermissionException ex = assertThrows(NoRolePermissionException.class,
                () -> eventService.createEvent(new Event(), 1L));

        assertTrue(ex.getMessage().contains("Non-Admins cannot create events."));
        verify(userService).findById(1L);
        verifyNoInteractions(eventRepository);
    }

    @Test
    void deleteEvent_event_exists() {
        Event e = new Event();
        e.setId(42L);

        when(eventRepository.findById(42L)).thenReturn(Optional.of(e));
        doNothing().when(eventRepository).delete(e);

        eventService.deleteEvent(42L);

        verify(eventRepository).findById(42L);
        verify(eventRepository).delete(e);
    }

    @Test
    void deleteEvent_not_found () {
        when(eventRepository.findById(6L)).thenReturn(Optional.empty());

        EventNotFoundException ex = assertThrows(EventNotFoundException.class,
                () -> eventService.deleteEvent(6L));

        assertTrue(ex.getMessage().contains("Event not found"));
        verify(eventRepository).findById(6L);
        verify(eventRepository, never()).delete(any());
    }
    @Test
    void updateEvent() {
        Event existing = new Event();
        existing.setId(20L);
        existing.setTitle("Old");
        existing.setDescription("old desc");
        existing.setImageUrl("old.jpg");
        existing.setEventTime(LocalDateTime.of(2023, 1, 1, 12, 0));

        Event updated = new Event();
        updated.setTitle("New");
        updated.setDescription("new desc");
        updated.setImageUrl("new.jpg");
        updated.setEventTime(LocalDateTime.of(2025, 2, 2, 15, 30));

        when(eventRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(updated, 20L);

        assertEquals("New", result.getTitle());
        assertEquals("new desc", result.getDescription());
        assertEquals("new.jpg", result.getImageUrl());
        assertEquals(LocalDateTime.of(2025, 2, 2, 15, 30), result.getEventTime());

        verify(eventRepository).findById(20L);
        verify(eventRepository).save(existing);
    }

    @Test
    void updateEvent_not_found() {
        when(eventRepository.findById(30L)).thenReturn(Optional.empty());

        EventNotFoundException ex = assertThrows(EventNotFoundException.class,
                () -> eventService.updateEvent(new Event(), 30L));

        assertTrue(ex.getMessage().contains("Event not found"));
        verify(eventRepository).findById(30L);
        verify(eventRepository, never()).save(any());
    }
    @Test
    void hideEvent() {
        Event e = new Event();
        e.setId(40L);
        e.setHidden(false);

        when(eventRepository.findById(40L)).thenReturn(Optional.of(e));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.hideEvent(40L);

        assertTrue(result.isHidden());
        verify(eventRepository).findById(40L);
        verify(eventRepository).save(e);
    }
    @Test
    void hideEvent_not_found() {
        when(eventRepository.findById(41L)).thenReturn(Optional.empty());

        EventNotFoundException ex = assertThrows(EventNotFoundException.class,
                () -> eventService.hideEvent(41L));

        assertTrue(ex.getMessage().contains("Event not found"));
        verify(eventRepository).findById(41L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void unHideEvent() {
        Event e = new Event();
        e.setId(50L);
        e.setHidden(true);

        when(eventRepository.findById(50L)).thenReturn(Optional.of(e));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.unHideEvent(50L);

        assertFalse(result.isHidden());
        verify(eventRepository).findById(50L);
        verify(eventRepository).save(e);

    }

    @Test
    void unHideEvent_not_found() {
        when(eventRepository.findById(51L)).thenReturn(Optional.empty());

        EventNotFoundException ex = assertThrows(EventNotFoundException.class,
                () -> eventService.unHideEvent(51L));

        assertTrue(ex.getMessage().contains("Event not found"));
        verify(eventRepository).findById(51L);
        verify(eventRepository, never()).save(any());
    }

}
