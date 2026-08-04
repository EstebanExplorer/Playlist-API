package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.application.auth.usecase.AuthenticateUserUseCase;
import com.esteban.playlistapi.presentation.dto.error.ApiErrorResponse;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.esteban.playlistapi.presentation.dto.response.AuthResponse;
import com.esteban.playlistapi.presentation.mapper.AuthPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Controlador REST para la gestión de autenticación e identidad de usuarios (UC-001).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión e identidad de usuarios (UC-001)")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final AuthPresentationMapper authPresentationMapper;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                          AuthPresentationMapper authPresentationMapper) {
        this.authenticateUserUseCase = Objects.requireNonNull(authenticateUserUseCase, "authenticateUserUseCase no puede ser nulo");
        this.authPresentationMapper = Objects.requireNonNull(authPresentationMapper, "authPresentationMapper no puede ser nulo");
    }

    @PostMapping("/login")
    @SecurityRequirements // Endpoint público sin requesito de JWT
    @Operation(summary = "Iniciar sesión de usuario", description = "Autentica al usuario mediante email y contraseña, retornando un token JWT de acceso válido por 24 horas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o campos malformados",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (Email o contraseña errónea)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticateUserCommand command = authPresentationMapper.toCommand(request);
        AuthenticationResult result = authenticateUserUseCase.execute(command);
        AuthResponse response = authPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
