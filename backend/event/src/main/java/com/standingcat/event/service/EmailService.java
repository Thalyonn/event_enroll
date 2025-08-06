package com.standingcat.event.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    public void sendEnrollmentConfirmation(String toEmail, String eventTitle) {
        //WIP
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("realemail@something.com"); //change this to match email property later
        message.setSubject("Event Registration Confirmation");
        message.setTo(toEmail);
        message.setText("Your registration to " + eventTitle + " has been confirmed."); //fill this with more info later
        try {
            mailSender.send(message);
            System.out.println("Enrollment confirmation email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            //reminder: log the error properly in a real application
        }
    }
}
