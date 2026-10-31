package com.subscriptiq.common.exception;

/**
 * Thrown when the client sends an invalid or malformed request.
 * Results in HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}

