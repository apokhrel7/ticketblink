package com.ticketblink.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static ApiException notFound(String resource, Object id) {
        return new ApiException(resource + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public static ApiException conflict(String message) {
        return new ApiException(message, HttpStatus.CONFLICT);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(message, HttpStatus.BAD_REQUEST);
    }
}
