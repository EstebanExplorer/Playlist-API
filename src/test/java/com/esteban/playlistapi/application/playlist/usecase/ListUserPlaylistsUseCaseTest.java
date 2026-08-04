package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.ListUserPlaylistsQuery;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUserPlaylistsUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    private ListUserPlaylistsUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist1;
    private Playlist mockPlaylist2;

    @BeforeEach
    void setUp() {
        useCase = new ListUserPlaylistsUseCase(playlistRepository);
        mockPlaylist1 = Playlist.create("Favoritas Rock", userId);
        mockPlaylist2 = Playlist.create("Relax Instrumental", userId);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe retornar la lista de PlaylistResult mapeada correctamente cuando el usuario tiene playlists")
        void shouldReturnListOfPlaylistResults_whenUserHasPlaylists() {
            // Arrange
            ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
            when(playlistRepository.findByUserId(userId)).thenReturn(List.of(mockPlaylist1, mockPlaylist2));

            // Act
            List<PlaylistResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(mockPlaylist1.getId(), results.get(0).id());
            assertEquals("Favoritas Rock", results.get(0).name());
            assertEquals(userId, results.get(0).userId());

            assertEquals(mockPlaylist2.getId(), results.get(1).id());
            assertEquals("Relax Instrumental", results.get(1).name());
            assertEquals(userId, results.get(1).userId());

            verify(playlistRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando el usuario no tiene ninguna playlist creada")
        void shouldReturnEmptyList_whenUserHasNoPlaylists() {
            // Arrange
            ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
            when(playlistRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

            // Act
            List<PlaylistResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());

            verify(playlistRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe retornar una lista inmutable que lanza UnsupportedOperationException al intentar modificarla")
        void shouldReturnUnmodifiableList_andThrowUnsupportedOperationException_whenMutatingResult() {
            // Arrange
            ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
            when(playlistRepository.findByUserId(userId)).thenReturn(List.of(mockPlaylist1));
            PlaylistResult dummyResult = new PlaylistResult(UUID.randomUUID(), "Nueva", userId, 0, LocalDateTime.now(), LocalDateTime.now());

            // Act
            List<PlaylistResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertThrows(UnsupportedOperationException.class, () -> results.add(dummyResult));
            assertThrows(UnsupportedOperationException.class, () -> results.clear());

            verify(playlistRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe tratar el retorno nulo del repositorio de forma segura y retornar una lista vacía")
        void shouldHandleNullFromRepository_andReturnEmptyList() {
            // Arrange
            ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
            when(playlistRepository.findByUserId(userId)).thenReturn(null);

            // Act
            List<PlaylistResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());

            verify(playlistRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(playlistRepository);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar InvalidQueryException cuando la consulta enviada a execute es nula")
        void shouldThrowInvalidQueryException_whenQueryIsNull() {
            // Act & Assert
            InvalidQueryException exception = assertThrows(
                    InvalidQueryException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("La consulta para listar las playlists no puede ser nula.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidQueryException al instanciar la consulta con userId nulo")
        void shouldThrowInvalidQueryException_whenUserIdInQueryIsNull() {
            // Act & Assert
            InvalidQueryException exception = assertThrows(
                    InvalidQueryException.class,
                    () -> new ListUserPlaylistsQuery(null)
            );

            assertEquals("El ID del usuario no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al instanciar el UseCase con dependencias nulas")
        void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> new ListUserPlaylistsUseCase(null)
            );
        }
    }
}
