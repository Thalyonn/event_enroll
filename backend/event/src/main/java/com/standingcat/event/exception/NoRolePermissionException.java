package com.standingcat.event.exception;

public class NoRolePermissionException extends RuntimeException{
    public NoRolePermissionException(String message) {
        super(message);
    }
}
