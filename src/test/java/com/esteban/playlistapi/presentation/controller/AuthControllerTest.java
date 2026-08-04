package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.application.auth.usecase.AuthenticateUserUseCase;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.esteban.playlistapi.presentation.dto.response.AuthResponse;
import com.esteban.playlistapi.presentation.mapper.AuthPresentationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockBean
    private AuthPresentationMapper authPresentationMapper;

    @Nested
    @DisplayName("Pruebas del Endpoint POST /api/v1/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con AuthResponse comprobando la secuencia de ejecución exacta (InOrder)")
        void shouldReturnHttp200AndAuthResponse_whenCredentialsAreValid() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@example.com", "Password123!");
            AuthenticateUserCommand command = new AuthenticateUserCommand("user@example.com", "Password123!");
            AuthenticationResult result = new AuthenticationResult("mock.jwt.token", "Bearer", 86400L);
            AuthResponse response = AuthResponse.bearer("mock.jwt.token", 86400L);

            when(authPresentationMapper.toCommand(any(LoginRequest.class))).thenReturn(command);
            when(authenticateUserUseCase.execute(any(AuthenticateUserCommand.class))).thenReturn(result);
            when(authPresentationMapper.toResponse(any(AuthenticationResult.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresInSeconds").value(86400));

            // Verificación explícita de secuencia ordenada con InOrder
            InOrder inOrder = inOrder(authPresentationMapper, authenticateUserUseCase);
            inOrder.verify(authPresentationMapper).toCommand(any(LoginRequest.class));
            inOrder.verify(authenticateUserUseCase).execute(any(AuthenticateUserCommand.class));
            inOrder.verify(authPresentationMapper).toResponse(any(AuthenticationResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(authPresentationMapper, authenticateUserUseCase);
        }

        @Test
        @DisplayName("Debe retornar HTTP 400 Bad Request sin interactuar con los servicios cuando el email es inválido")
        void shouldReturnHttp400_whenEmailFormatIsInvalid() throws Exception {
            // Arrange
            LoginRequest invalidRequest = new LoginRequest("invalid-email-format", "Password123!");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authPresentationMapper, authenticateUserUseCase);
        }

        @Test
        @DisplayName("Debe retornar HTTP 400 Bad Request sin interactuar con los servicios cuando la contraseña está vacía")
        void shouldReturnHttp400_whenPasswordIsEmpty() throws Exception {
            // Arrange
            LoginRequest invalidRequest = new LoginRequest("user@example.com", "");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authPresentationMapper, authenticateUserUseCase);
        }
    }

    @Nested
    @DisplayName("Pruebas de Inyección por Constructor (Fail-Fast)")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si alguna dependencia es nula")
        void shouldThrowNullPointerException_whenDependenciesAreNull() {
            assertThrows(NullPointerException.class, () -> new AuthController(null, authPresentationMapper));
            assertThrows(NullPointerException.class, () -> new AuthController(authenticateUserUseCase, null));
        }
    }
}
