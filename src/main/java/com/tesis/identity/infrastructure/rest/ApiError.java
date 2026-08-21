package com.tesis.identity.infrastructure.rest;

import java.time.Instant;

/**
 * Forma uniforme de cualquier error devuelto por la API.
 * "error" se mantiene como nombre de campo por compatibilidad con el
 * frontend actual, que ya lee ese campo en las respuestas de error.
 */
public record ApiError(String error, int status, Instant timestamp) {

    public static ApiError of(String error, int status) {
        return new ApiError(error, status, Instant.now());
    }
}
