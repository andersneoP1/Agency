package com.nativetravel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при конфликте уникальности данных (например, дубликат email).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
