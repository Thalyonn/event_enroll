package com.standingcat.event.dto;

import com.standingcat.event.model.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private LocalDateTime enrollmentTime;
    private Long userId;
    private String email;
    private String username;
    private Long eventId;


    public EnrollmentResponse(Enrollment enrollment){
        this.id = enrollment.getId();
        this.enrollmentTime=enrollment.getEnrollmentTime();
        this.userId=enrollment.getUser().getId();
        this.email=enrollment.getUser().getEmail();
        this.username=enrollment.getUser().getUsername();
        this.eventId=enrollment.getEvent().getId();
    }


}
