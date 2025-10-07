package com.standingcat.event.dto;

import com.standingcat.event.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime eventTime;
    private Integer capacity;
    private int currentEnrollments;

    public EventResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.imageUrl = event.getImageUrl();
        this.eventTime = event.getEventTime();
        this.capacity = event.getCapacity();
        this.currentEnrollments = event.getEnrollments().size();
    }
}
