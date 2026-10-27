package com.aurum.main.exception;

public class EventHasBeenCompletedException extends RuntimeException {
    public EventHasBeenCompletedException(String message) {
        super(message);
    }
}
