package com.standingcat.event.exception;

public class UsernameAlreadyTakenException extends RuntimeException{
    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}
