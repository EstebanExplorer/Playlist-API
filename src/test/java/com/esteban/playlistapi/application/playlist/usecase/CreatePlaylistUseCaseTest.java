package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.CreatePlaylistCommand;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.domain.exception.InvalidPlaylistNameException;
import com.esteban.playlistapi.domain.exception.UserNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePlaylistUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Playlist> playlistCaptor;

    private CreatePlaylistUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final String validName = "Mis Canciones Favoritas 2026";
    private User mockUser;

    @BeforeEach
    void setUp() {
        useCase = new CreatePlaylistUseCase(playlistRepository, userRepository);
        mockUser = new User(userId, "usuario_demo", "password_hash", "usuario@email.com");
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe crear y guardar una playlist correctamente cuando el usuario existe y el nombre es válido")
        void shouldCreatePlaylistSuccessfully() {
            // Arrange
            CreatePlaylistCommand command = new CreatePlaylistCommand(validName, userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaylistResult result = useCase.execute(command);

            // Assert
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(validName, result.name());
            assertEquals(userId, result.userId());
            assertEquals(0, result.songCount());
            assertNotNull(result.createdAt());
            assertNotNull(result.updatedAt());

            verify(userRepository, times(1)).findById(userId);
            verify(playlistRepository, times(1)).save(playlistCaptor.capture());

            Playlist capturedPlaylist = playlistCaptor.getValue();
            assertEquals(validName, capturedPlaylist.getName().getValue());
            assertEquals(userId, capturedPlaylist.getUserId());
            assertFalse(capturedPlaylist.getDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe en el sistema")
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            // Arrange
            CreatePlaylistCommand command = new CreatePlaylistCommand(validName, userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(userId.toString()));
            verify(userRepository, times(1)).findById(userId);
            verify(playlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar InvalidPlaylistNameException cuando el nombre excede los 100 caracteres permitidos")
        void shouldThrowInvalidPlaylistNameException_whenNameExceedsMaxLength() {
            // Arrange
            String longName = "A".repeat(101);
            CreatePlaylistCommand invalidNameCommand = new CreatePlaylistCommand(longName, userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            // Act & Assert
            assertThrows(
                    InvalidPlaylistNameException.class,
                    () -> useCase.execute(invalidNameCommand)
            );

            verify(userRepository, times(1)).findById(userId);
            verify(playlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException cuando el comando execute es nulo")
        void shouldThrowInvalidCommandException_whenCommandIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("El comando para crear la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(userRepository, playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al instanciar un comando con campos nulos o vacíos")
        void shouldThrowInvalidCommandException_whenCommandFieldsAreInvalid() {
            // Act & Assert - Nombre nulo
            assertThrows(
                    InvalidCommandException.class,
                    () -> new CreatePlaylistCommand(null, userId)
            );

            // Act & Assert - Nombre vacío
            assertThrows(
                    InvalidCommandException.class,
                    () -> new CreatePlaylistCommand("   ", userId)
            );

            // Act & Assert - User ID nulo
            assertThrows(
                    InvalidCommandException.class,
                    () -> new CreatePlaylistCommand("Mi Playlist", null)
            );
        }

        @Test
        @DisplayName("Debe propagar ExternalServiceException cuando la persistencia en el repositorio falla")
        void shouldPropagateExternalServiceException_whenRepositoryFailsToSave() {
            // Arrange
            CreatePlaylistCommand command = new CreatePlaylistCommand(validName, userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(playlistRepository.save(any(Playlist.class)))
                    .thenThrow(new ExternalServiceException("PlaylistRepository", "Error de conexión a la base de datos"));

            // Act & Assert
            ExternalServiceException exception = assertThrows(
                    ExternalServiceException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains("PlaylistRepository"));
            verify(userRepository, times(1)).findById(userId);
            verify(playlistRepository, times(1)).save(any(Playlist.class));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al instanciar el UseCase con dependencias nulas")
        void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
            // Act & Assert - Repositorio de Playlist nulo
            assertThrows(
                    NullPointerException.class,
                    () -> new CreatePlaylistUseCase(null, userRepository)
            );

            // Act & Assert - Repositorio de Usuario nulo
            assertThrows(
                    NullPointerException.class,
                    () -> new CreatePlaylistUseCase(playlistRepository, null)
            );
        }
    }
}
