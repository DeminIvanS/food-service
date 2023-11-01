package ru.liga.handler;

public class EntityException extends RuntimeException{

    private final StatusException statusException;

    public EntityException(StatusException statusException) {

        this.statusException = statusException;
    }
    public StatusException getStatusException() {

        return statusException;
    }
}
