package com.esteban.playlistapi.application.auth.usecase;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.application.auth.exception.InvalidCredentialsException;
import com.esteban.playlistapi.application.auth.port.PasswordEncoderPort;
import com.esteban.playlistapi.application.auth.port.TokenProviderPort;
import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private AuthenticateUserUseCase authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        authenticateUserUseCase = new AuthenticateUserUseCase(
                userRepository,
                passwordEncoderPort,
                tokenProviderPort
        );
    }

    @Test
    @DisplayName("UC-001: Autenticación exitosa debe retornar AuthenticationResult con token Bearer")
    void authenticateSuccessfully() {
        User user = User.create("esteban", "esteban@example.com", "$2a$10$encodedPasswordHash");
        AuthenticateUserCommand command = new AuthenticateUserCommand("esteban", "secret123");

        when(userRepository.findByUsername("esteban")).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("secret123", "$2a$10$encodedPasswordHash")).thenReturn(true);
        when(tokenProviderPort.generateToken(user)).thenReturn("mocked.jwt.token");
        when(tokenProviderPort.getExpirationTimeSeconds()).thenReturn(86400L);

        AuthenticationResult result = authenticateUserUseCase.execute(command);

        assertNotNull(result);
        assertEquals("mocked.jwt.token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(86400L, result.expiresIn());

        verify(userRepository, times(1)).findByUsername("esteban");
        verify(passwordEncoderPort, times(1)).matches("secret123", "$2a$10$encodedPasswordHash");
        verify(tokenProviderPort, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("UC-001: Debe lanzar InvalidCredentialsException si el usuario no existe")
    void authenticateFailsUserNotFound() {
        AuthenticateUserCommand command = new AuthenticateUserCommand("nonexistent", "secret123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticateUserUseCase.execute(command));

        verify(passwordEncoderPort, never()).matches(any(), any());
        verify(tokenProviderPort, never()).generateToken(any());
    }

    @Test
    @DisplayName("UC-001: Debe lanzar InvalidCredentialsException si la contraseña no coincide")
    void authenticateFailsWrongPassword() {
        User user = User.create("esteban", "esteban@example.com", "$2a$10$encodedPasswordHash");
        AuthenticateUserCommand command = new AuthenticateUserCommand("esteban", "wrongpassword");

        when(userRepository.findByUsername("esteban")).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("wrongpassword", "$2a$10$encodedPasswordHash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authenticateUserUseCase.execute(command));

        verify(tokenProviderPort, never()).generateToken(any());
    }
}
