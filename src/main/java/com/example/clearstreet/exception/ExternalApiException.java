package com.example.clearstreet.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends RuntimeException {

    private final HttpStatus status;

    public ExternalApiException(String message) {
        this(message, HttpStatus.BAD_GATEWAY);
    }

    public ExternalApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ExternalApiException(String message, Throwable cause) {
        this(message, HttpStatus.BAD_GATEWAY, cause);
    }

    public ExternalApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
