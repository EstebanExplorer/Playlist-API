package com.esteban.playlistapi.presentation.mapper;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.esteban.playlistapi.presentation.dto.response.AuthResponse;

import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Mapper de presentación para el flujo de autenticación e identidad (UC-001).
 * Traduce entre los DTOs HTTP (Presentation) y los DTOs internos (Application).
 * Pertenece exclusivamente a la capa Presentation.
 */
@Component
public class AuthPresentationMapper {

    public AuthenticateUserCommand toCommand(LoginRequest request) {
        Objects.requireNonNull(request, "request no puede ser nulo");
        return new AuthenticateUserCommand(request.email(), request.password());
    }

    public AuthResponse toResponse(AuthenticationResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");
        return AuthResponse.bearer(result.accessToken(), result.expiresIn());
    }
}
