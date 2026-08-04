package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.DeletePlaylistCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePlaylistUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    private DeletePlaylistUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;

    @BeforeEach
    void setUp() {
        useCase = new DeletePlaylistUseCase(playlistRepository);
        mockPlaylist = Playlist.create("Playlist a Eliminar", userId);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe eliminar la playlist exitosamente cuando la playlist existe y el comando es válido")
        void shouldDeletePlaylistSuccessfully_whenPlaylistExistsAndCommandIsValid() {
            // Arrange
            DeletePlaylistCommand command = new DeletePlaylistCommand(mockPlaylist.getId());
            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            doNothing().when(playlistRepository).delete(mockPlaylist);

            // Act
            useCase.execute(command);

            // Assert
            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(playlistRepository, times(1)).delete(mockPlaylist);
            verifyNoMoreInteractions(playlistRepository);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar PlaylistNotFoundException cuando la playlist no existe en el repositorio")
        void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            DeletePlaylistCommand command = new DeletePlaylistCommand(nonExistentId);

            when(playlistRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(nonExistentId.toString()));
            verify(playlistRepository, times(1)).findById(nonExistentId);
            verify(playlistRepository, never()).delete(any());
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException cuando el comando recibido es nulo")
        void shouldThrowInvalidCommandException_whenCommandIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("El comando para eliminar la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con playlistId nulo")
        void shouldThrowInvalidCommandException_whenPlaylistIdIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> new DeletePlaylistCommand(null)
            );

            assertEquals("El ID de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si las dependencias del constructor son nulas")
        void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new DeletePlaylistUseCase(null));
        }
    }
}
