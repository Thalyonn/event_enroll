package com.standingcat.event.exception;

public class EmailAlreadyRegisteredException extends RuntimeException{
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
