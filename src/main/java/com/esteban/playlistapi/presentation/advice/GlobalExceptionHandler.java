package com.esteban.playlistapi.presentation.advice;

import com.esteban.playlistapi.application.auth.exception.InvalidCredentialsException;
import com.esteban.playlistapi.domain.exception.DuplicateSongException;
import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.domain.exception.InvalidPlaylistNameException;
import com.esteban.playlistapi.domain.exception.InvalidSongDataException;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.exception.UnauthorizedPlaylistAccessException;
import com.esteban.playlistapi.domain.exception.UserNotFoundException;
import com.esteban.playlistapi.presentation.dto.error.ApiErrorResponse;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Interceptor y Manejador Global de Excepciones para la capa Presentation.
 * Traduce excepciones de Domain y Application a respuestas HTTP estandarizadas (RFC 7807 / Problem Details).
 * Pertenece exclusivamente a la capa Presentation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------------
    // 1. AUTENTICACIÓN Y SEGURIDAD (401 Unauthorized)
    // ------------------------------------------------------------------------

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 2. AUTORIZACIÓN (403 Forbidden)
    // ------------------------------------------------------------------------

    @ExceptionHandler(UnauthorizedPlaylistAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedPlaylistAccess(
            UnauthorizedPlaylistAccessException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 3. RECURSOS NO ENCONTRADOS (404 Not Found)
    // ------------------------------------------------------------------------

    @ExceptionHandler({
            PlaylistNotFoundException.class,
            SongNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 4. CONFLICTOS DE RECURSOS (409 Conflict)
    // ------------------------------------------------------------------------

    @ExceptionHandler(DuplicateSongException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSong(
            DuplicateSongException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 5. ERRORES DE SINTAXIS Y VALIDACIÓN (400 Bad Request)
    // ------------------------------------------------------------------------

    @ExceptionHandler({
            InvalidCommandException.class,
            InvalidQueryException.class,
            InvalidPlaylistNameException.class,
            InvalidSongDataException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldErrorDetails> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ApiErrorResponse.FieldErrorDetails(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            ));
        }

        return buildValidationErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Error de validación en la solicitud HTTP.",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 6. ERRORES DE SERVICIOS EXTERNOS (502 Bad Gateway)
    // ------------------------------------------------------------------------

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalService(
            ExternalServiceException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // ------------------------------------------------------------------------
    // 7. ERRORES NO CONTROLADOS DEL SERVIDOR (500 Internal Server Error)
    // ------------------------------------------------------------------------

    @ExceptionHandler({NullPointerException.class, Exception.class})
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(
            Exception ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor. Por favor consulte al administrador.",
                request
        );
    }

    // ------------------------------------------------------------------------
    // MÉTODOS AUXILIARES PRIVADOS DE CONSTRUCCIÓN (DRY)
    // ------------------------------------------------------------------------

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ApiErrorResponse> buildValidationErrorResponse(
            HttpStatus status, String message, HttpServletRequest request, List<ApiErrorResponse.FieldErrorDetails> validationErrors) {
        ApiErrorResponse error = ApiErrorResponse.ofValidation(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(status).body(error);
    }
}
