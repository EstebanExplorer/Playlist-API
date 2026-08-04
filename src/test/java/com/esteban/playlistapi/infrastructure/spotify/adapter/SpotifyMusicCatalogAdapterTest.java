package com.esteban.playlistapi.infrastructure.spotify.adapter;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.infrastructure.spotify.client.SpotifyClient;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyAlbumItem;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyArtistItem;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;
import com.esteban.playlistapi.infrastructure.spotify.mapper.SpotifyMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyMusicCatalogAdapterTest {

    @Mock
    private SpotifyClient spotifyClient;

    @Spy
    private SpotifyMapper spotifyMapper;

    private SpotifyMusicCatalogAdapter adapter;

    private SpotifyTrackItem mockTrackItem;

    @BeforeEach
    void setUp() {
        adapter = new SpotifyMusicCatalogAdapter(spotifyClient, spotifyMapper);

        SpotifyArtistItem artist = new SpotifyArtistItem("art-1", "Pink Floyd");
        SpotifyAlbumItem album = new SpotifyAlbumItem("alb-1", "The Dark Side of the Moon", List.of());
        mockTrackItem = new SpotifyTrackItem("spot-555", "Time", 413000L, List.of(artist), album);
    }

    @Test
    @DisplayName("Debe delegar a SpotifyClient.fetchTrackById y mapear el resultado a una entidad Song")
    void shouldGetSongBySpotifyIdAndMapToDomain() {
        // Arrange
        when(spotifyClient.fetchTrackById("spot-555")).thenReturn(Optional.of(mockTrackItem));

        // Act
        Optional<Song> songOpt = adapter.getSongBySpotifyId("spot-555");

        // Assert
        assertTrue(songOpt.isPresent());
        assertEquals("spot-555", songOpt.get().getSpotifyId());
        assertEquals("Time", songOpt.get().getTitle().getValue());
        assertEquals("Pink Floyd", songOpt.get().getArtist().getValue());
        assertEquals("The Dark Side of the Moon", songOpt.get().getAlbum().getValue());
        assertEquals(413, songOpt.get().getDuration().getSeconds());

        verify(spotifyClient, times(1)).fetchTrackById("spot-555");
        verify(spotifyMapper, times(1)).toDomain(mockTrackItem);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() sin invocar a SpotifyClient cuando spotifyId es nulo o en blanco")
    void shouldReturnEmptyOptional_whenSpotifyIdIsBlank() {
        assertTrue(adapter.getSongBySpotifyId(null).isEmpty());
        assertTrue(adapter.getSongBySpotifyId("   ").isEmpty());
        verifyNoInteractions(spotifyClient, spotifyMapper);
    }

    @Test
    @DisplayName("Debe buscar canciones y retornar lista mapeada de entidades Song del dominio")
    void shouldSearchSongsAndMapToListOfDomainSongs() {
        // Arrange
        when(spotifyClient.searchTracks("Pink Floyd", 10)).thenReturn(List.of(mockTrackItem));

        // Act
        List<Song> songs = adapter.searchSongs("Pink Floyd", 10);

        // Assert
        assertNotNull(songs);
        assertEquals(1, songs.size());
        assertEquals("Time", songs.get(0).getTitle().getValue());

        verify(spotifyClient, times(1)).searchTracks("Pink Floyd", 10);
        verify(spotifyMapper, times(1)).toDomain(mockTrackItem);
    }

    @Test
    @DisplayName("Debe propagar ExternalServiceException cuando la llamada a SpotifyClient falla")
    void shouldPropagateExternalServiceException_whenSpotifyClientFails() {
        // Arrange
        when(spotifyClient.searchTracks(anyString(), anyInt()))
                .thenThrow(new ExternalServiceException("Spotify API", "Timeout de conexión"));

        // Act & Assert
        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> adapter.searchSongs("Error Query", 5)
        );

        assertTrue(exception.getMessage().contains("Spotify API"));
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException al instanciar con dependencias nulas (Fail-Fast)")
    void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
        assertThrows(NullPointerException.class, () -> new SpotifyMusicCatalogAdapter(null, spotifyMapper));
        assertThrows(NullPointerException.class, () -> new SpotifyMusicCatalogAdapter(spotifyClient, null));
    }
}
