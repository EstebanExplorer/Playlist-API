package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.GetPlaylistByIdQuery;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPlaylistByIdUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    private GetPlaylistByIdUseCase useCase;

    private final UUID playlistId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String playlistName = "Rock Clásico 80s";
    private Playlist mockPlaylist;

    @BeforeEach
    void setUp() {
        useCase = new GetPlaylistByIdUseCase(playlistRepository);
        mockPlaylist = Playlist.create(playlistName, userId);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe retornar la playlist mapeada a PlaylistResult cuando existe en el repositorio")
        void shouldReturnPlaylistResult_whenPlaylistExists() {
            // Arrange
            GetPlaylistByIdQuery query = new GetPlaylistByIdQuery(playlistId);
            when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(mockPlaylist));

            // Act
            PlaylistResult result = useCase.execute(query);

            // Assert
            assertNotNull(result);
            assertEquals(mockPlaylist.getId(), result.id());
            assertEquals(playlistName, result.name());
            assertEquals(userId, result.userId());
            assertEquals(0, result.songCount());
            assertNotNull(result.createdAt());
            assertNotNull(result.updatedAt());

            verify(playlistRepository, times(1)).findById(playlistId);
            verifyNoMoreInteractions(playlistRepository);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar PlaylistNotFoundException cuando el repositorio retorna vacío")
        void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
            // Arrange
            GetPlaylistByIdQuery query = new GetPlaylistByIdQuery(playlistId);
            when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(query)
            );

            assertTrue(exception.getMessage().contains(playlistId.toString()));
            verify(playlistRepository, times(1)).findById(playlistId);
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidQueryException cuando la consulta enviada a execute es nula")
        void shouldThrowInvalidQueryException_whenQueryIsNull() {
            // Act & Assert
            InvalidQueryException exception = assertThrows(
                    InvalidQueryException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("La consulta para obtener la playlist no puede ser nula.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidQueryException al instanciar la consulta con playlistId nulo")
        void shouldThrowInvalidQueryException_whenPlaylistIdInQueryIsNull() {
            // Act & Assert
            InvalidQueryException exception = assertThrows(
                    InvalidQueryException.class,
                    () -> new GetPlaylistByIdQuery(null)
            );

            assertEquals("El ID de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al instanciar el UseCase con dependencias nulas")
        void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> new GetPlaylistByIdUseCase(null)
            );
        }
    }
}
