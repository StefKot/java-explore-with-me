package ru.practicum.mainservice.exception;

public class TimeLessTwoHourException extends RuntimeException {
    public TimeLessTwoHourException(String message) {
        super(message);
    }
}