package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.AddSongCommand;
import com.esteban.playlistapi.domain.exception.DuplicateSongException;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.service.PlaylistDomainService;
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
class AddSongToPlaylistUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private MusicCatalogPort musicCatalogPort;

    @Mock
    private PlaylistDomainService playlistDomainService;

    private AddSongToPlaylistUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;
    private Song mockSong;
    private final String spotifySongId = "spotify-track-12345";

    @BeforeEach
    void setUp() {
        useCase = new AddSongToPlaylistUseCase(playlistRepository, musicCatalogPort, playlistDomainService);
        mockPlaylist = Playlist.create("Mi Playlist Rock", userId);
        mockSong = Song.create(spotifySongId, "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe agregar la canción a la playlist exitosamente y guardar los cambios")
        void shouldAddSongToPlaylistSuccessfully_whenPlaylistAndSongExist() {
            // Arrange
            AddSongCommand command = new AddSongCommand(mockPlaylist.getId(), spotifySongId);

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(musicCatalogPort.getSongBySpotifyId(spotifySongId)).thenReturn(Optional.of(mockSong));
            when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaylistResult result = useCase.execute(command);

            // Assert
            assertNotNull(result);
            assertEquals(mockPlaylist.getId(), result.id());
            assertEquals(1, result.songCount());

            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(musicCatalogPort, times(1)).getSongBySpotifyId(spotifySongId);
            verify(playlistRepository, times(1)).save(mockPlaylist);
            verifyNoMoreInteractions(playlistRepository, musicCatalogPort);
            verifyNoInteractions(playlistDomainService);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar PlaylistNotFoundException cuando la playlist no existe")
        void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            AddSongCommand command = new AddSongCommand(nonExistentId, spotifySongId);

            when(playlistRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(nonExistentId.toString()));
            verify(playlistRepository, times(1)).findById(nonExistentId);
            verifyNoInteractions(musicCatalogPort);
            verify(playlistRepository, never()).save(any());
            verifyNoMoreInteractions(playlistRepository);
            verifyNoInteractions(playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar SongNotFoundException cuando la canción no existe en el catálogo")
        void shouldThrowSongNotFoundException_whenSongDoesNotExistInCatalog() {
            // Arrange
            AddSongCommand command = new AddSongCommand(mockPlaylist.getId(), spotifySongId);

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(musicCatalogPort.getSongBySpotifyId(spotifySongId)).thenReturn(Optional.empty());

            // Act & Assert
            SongNotFoundException exception = assertThrows(
                    SongNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(spotifySongId));
            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(musicCatalogPort, times(1)).getSongBySpotifyId(spotifySongId);
            verify(playlistRepository, never()).save(any());
            verifyNoMoreInteractions(playlistRepository, musicCatalogPort);
            verifyNoInteractions(playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar DuplicateSongException cuando la canción ya existe en la playlist")
        void shouldThrowDuplicateSongException_whenSongIsAlreadyInPlaylist() {
            // Arrange
            mockPlaylist.addSong(mockSong); // agregamos la canción previamente
            AddSongCommand command = new AddSongCommand(mockPlaylist.getId(), spotifySongId);

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(musicCatalogPort.getSongBySpotifyId(spotifySongId)).thenReturn(Optional.of(mockSong));

            // Act & Assert
            DuplicateSongException exception = assertThrows(
                    DuplicateSongException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains("Bohemian Rhapsody"));
            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(musicCatalogPort, times(1)).getSongBySpotifyId(spotifySongId);
            verify(playlistRepository, never()).save(any());
            verifyNoMoreInteractions(playlistRepository, musicCatalogPort);
            verifyNoInteractions(playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException cuando el comando recibido es nulo")
        void shouldThrowInvalidCommandException_whenCommandIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("El comando para agregar la canción a la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, musicCatalogPort, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con playlistId nulo")
        void shouldThrowInvalidCommandException_whenPlaylistIdIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> new AddSongCommand(null, spotifySongId)
            );

            assertEquals("El ID de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, musicCatalogPort, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con songId nulo o en blanco")
        void shouldThrowInvalidCommandException_whenSongIdIsEmptyOrNull() {
            // Act & Assert (null)
            InvalidCommandException exNull = assertThrows(
                    InvalidCommandException.class,
                    () -> new AddSongCommand(mockPlaylist.getId(), null)
            );
            assertEquals("El ID de la canción no puede estar vacío.", exNull.getMessage());

            // Act & Assert (blank)
            InvalidCommandException exBlank = assertThrows(
                    InvalidCommandException.class,
                    () -> new AddSongCommand(mockPlaylist.getId(), "   ")
            );
            assertEquals("El ID de la canción no puede estar vacío.", exBlank.getMessage());

            verifyNoInteractions(playlistRepository, musicCatalogPort, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si las dependencias del constructor son nulas")
        void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new AddSongToPlaylistUseCase(null, musicCatalogPort, playlistDomainService));
            assertThrows(NullPointerException.class, () -> new AddSongToPlaylistUseCase(playlistRepository, null, playlistDomainService));
            assertThrows(NullPointerException.class, () -> new AddSongToPlaylistUseCase(playlistRepository, musicCatalogPort, null));
        }
    }
}
