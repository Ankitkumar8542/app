package com.musicPlayer.app.common.exception;



public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}