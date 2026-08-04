package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.RemoveSongCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;
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
class RemoveSongFromPlaylistUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    private RemoveSongFromPlaylistUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;
    private Song mockSong;

    @BeforeEach
    void setUp() {
        useCase = new RemoveSongFromPlaylistUseCase(playlistRepository);
        mockPlaylist = Playlist.create("Mi Playlist Favorita", userId);
        mockSong = Song.create("spotify-track-001", "Hotel California", "Eagles", "Hotel California", 391);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe remover la canción de la playlist exitosamente, actualizar el estado del Aggregate y guardar los cambios")
        void shouldRemoveSongFromPlaylistSuccessfully_whenPlaylistAndSongExist() {
            // Arrange
            mockPlaylist.addSong(mockSong);
            assertEquals(1, mockPlaylist.getSongs().size());
            assertTrue(mockPlaylist.hasSong(mockSong.getId()));

            RemoveSongCommand command = new RemoveSongCommand(mockPlaylist.getId(), mockSong.getId());

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaylistResult result = useCase.execute(command);

            // Assert
            assertNotNull(result);
            assertEquals(mockPlaylist.getId(), result.id());
            assertEquals(0, result.songCount());

            // Verificación del cambio de estado interno en el Agregado
            assertEquals(0, mockPlaylist.getSongs().size());
            assertFalse(mockPlaylist.hasSong(mockSong.getId()));
            assertTrue(mockPlaylist.getSongs().stream().noneMatch(s -> s.getId().equals(mockSong.getId())));

            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(playlistRepository, times(1)).save(mockPlaylist);
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
            UUID nonExistentPlaylistId = UUID.randomUUID();
            RemoveSongCommand command = new RemoveSongCommand(nonExistentPlaylistId, mockSong.getId());

            when(playlistRepository.findById(nonExistentPlaylistId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(nonExistentPlaylistId.toString()));
            verify(playlistRepository, times(1)).findById(nonExistentPlaylistId);
            verify(playlistRepository, never()).save(any());
            verifyNoMoreInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar SongNotFoundException cuando la canción no pertenece a la playlist")
        void shouldThrowSongNotFoundException_whenSongIsNotInPlaylist() {
            // Arrange (mockPlaylist no contiene a mockSong)
            UUID songNotInPlaylist = UUID.randomUUID();
            RemoveSongCommand command = new RemoveSongCommand(mockPlaylist.getId(), songNotInPlaylist);

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));

            // Act & Assert
            SongNotFoundException exception = assertThrows(
                    SongNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(songNotInPlaylist.toString()));
            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(playlistRepository, never()).save(any());
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

            assertEquals("El comando para remover la canción de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con playlistId o songId nulo")
        void shouldThrowInvalidCommandException_whenPlaylistIdOrSongIdIsNull() {
            // Act & Assert (playlistId null)
            InvalidCommandException exPlaylistNull = assertThrows(
                    InvalidCommandException.class,
                    () -> new RemoveSongCommand(null, mockSong.getId())
            );
            assertEquals("El ID de la playlist no puede ser nulo.", exPlaylistNull.getMessage());

            // Act & Assert (songId null)
            InvalidCommandException exSongNull = assertThrows(
                    InvalidCommandException.class,
                    () -> new RemoveSongCommand(mockPlaylist.getId(), null)
            );
            assertEquals("El ID de la canción no puede ser nulo.", exSongNull.getMessage());

            verifyNoInteractions(playlistRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si la dependencia del constructor es nula")
        void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new RemoveSongFromPlaylistUseCase(null));
        }
    }
}
