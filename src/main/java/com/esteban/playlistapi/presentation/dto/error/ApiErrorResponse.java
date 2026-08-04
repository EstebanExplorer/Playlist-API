package com.esteban.playlistapi.presentation.dto.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO HTTP Response estandarizado para la representación estructurada de errores de la API.
 * Implementado como un Record Java 21 inmutable alineado a buenas prácticas de Problem Details (RFC 7807).
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetails> validationErrors
) {

    public record FieldErrorDetails(
            String field,
            String message
    ) {}

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }

    public static ApiErrorResponse ofValidation(int status, String error, String message, String path, List<FieldErrorDetails> validationErrors) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors != null ? validationErrors : List.of()
        );
    }
}
