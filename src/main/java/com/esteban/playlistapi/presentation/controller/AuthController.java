package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.application.auth.usecase.AuthenticateUserUseCase;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.esteban.playlistapi.presentation.dto.response.AuthResponse;
import com.esteban.playlistapi.presentation.mapper.AuthPresentationMapper;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Controlador REST para la gestión de autenticación e identidad de usuarios (UC-001).
 * <p>
 * Rol Arquitectónico:
 * Actúa estrictamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal y Clean Architecture.
 * Implementa el patrón Thin Controller.
 * <p>
 * Responsabilidad Exclusiva:
 * Orquestar el flujo unidireccional de datos en la frontera HTTP sin tomar ninguna decisión de negocio.
 * <p>
 * Pipeline de Procesamiento:
 * HTTP Request
 *      ↓
 * Jakarta Bean Validation (@Valid)
 *      ↓
 * Presentation Mapper (LoginRequest -> AuthenticateUserCommand)
 *      ↓
 * Application Layer (AuthenticateUserUseCase.execute)
 *      ↓
 * Presentation Mapper (AuthenticationResult -> AuthResponse)
 *      ↓
 * HTTP Response (200 OK + AuthResponse JSON)
 * <p>
 * Aislamiento:
 * No conoce ni importa clases del Dominio (Domain) ni de Infraestructura (Infrastructure).
 * Delegación total a la capa Application mediante Mappers.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final AuthPresentationMapper authPresentationMapper;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                          AuthPresentationMapper authPresentationMapper) {
        this.authenticateUserUseCase = Objects.requireNonNull(authenticateUserUseCase, "authenticateUserUseCase no puede ser nulo");
        this.authPresentationMapper = Objects.requireNonNull(authPresentationMapper, "authPresentationMapper no puede ser nulo");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticateUserCommand command = authPresentationMapper.toCommand(request);
        AuthenticationResult result = authenticateUserUseCase.execute(command);
        AuthResponse response = authPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
