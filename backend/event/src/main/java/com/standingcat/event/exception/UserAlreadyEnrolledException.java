package com.standingcat.event.exception;

public class UserAlreadyEnrolledException extends RuntimeException{
    public UserAlreadyEnrolledException(String message) {
        super(message);
    }
}
