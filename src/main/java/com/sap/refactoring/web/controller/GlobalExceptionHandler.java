/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.refactoring.web.controller;

import com.sap.refactoring.data.Error;
import java.util.Date;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles data integrity violations (e.g. email address already in use).
     *
     * @param e The exception
     * @return A 409 response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Error> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        final var error = new Error();
        error.setTimestamp(new Date());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles all generic data access exceptions.
     *
     * @param e The exception
     * @return A 500 response
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Error> handleDataAccessException(final DataAccessException e) {
        final var error = new Error();
        error.setTimestamp(new Date());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles illegal argument exceptions (e.g. updating user with invalid payload).
     *
     * @param e The exception
     * @return A 400 response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Error> handleIllegalArgumentException(final IllegalArgumentException e) {
        final var error = new Error();
        error.setTimestamp(new Date());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Generic exception handler.
     *
     * @param e The exception
     * @return A 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(final Exception e) {
        final var error = new Error();
        error.setTimestamp(new Date());
        error.setMessage(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
