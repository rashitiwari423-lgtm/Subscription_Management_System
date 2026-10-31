package com.subscriptiq.common.exception;

/**
 * Thrown when authentication fails or credentials are invalid.
 * Results in HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

