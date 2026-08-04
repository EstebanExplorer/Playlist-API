package com.esteban.playlistapi.infrastructure.spotify.mapper;

import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyAlbumItem;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyArtistItem;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyMapperTest {

    private SpotifyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SpotifyMapper();
    }

    @Test
    @DisplayName("Debe mapear un SpotifyTrackItem completo a una Entidad Song de Dominio")
    void shouldMapCompleteSpotifyTrackItemToDomainSong() {
        // Arrange
        SpotifyArtistItem artist1 = new SpotifyArtistItem("art-1", "Led Zeppelin");
        SpotifyArtistItem artist2 = new SpotifyArtistItem("art-2", "Jimmy Page");
        SpotifyAlbumItem album = new SpotifyAlbumItem("alb-1", "Led Zeppelin IV", List.of());
        SpotifyTrackItem item = new SpotifyTrackItem("spot-100", "Stairway to Heaven", 482000L, List.of(artist1, artist2), album);

        // Act
        Song song = mapper.toDomain(item);

        // Assert
        assertNotNull(song);
        assertNotNull(song.getId());
        assertEquals("spot-100", song.getSpotifyId());
        assertEquals("Stairway to Heaven", song.getTitle().getValue());
        assertEquals("Led Zeppelin, Jimmy Page", song.getArtist().getValue());
        assertEquals("Led Zeppelin IV", song.getAlbum().getValue());
        assertEquals(482, song.getDuration().getSeconds());
    }

    @Test
    @DisplayName("Debe utilizar valores por defecto cuando artistas o álbum son nulos o vacíos")
    void shouldFallbackToDefaults_whenArtistsOrAlbumAreNullOrEmpty() {
        // Arrange
        SpotifyTrackItem item = new SpotifyTrackItem("spot-200", "Solo Track", 120000L, List.of(), null);

        // Act
        Song song = mapper.toDomain(item);

        // Assert
        assertNotNull(song);
        assertEquals("spot-200", song.getSpotifyId());
        assertEquals("Solo Track", song.getTitle().getValue());
        assertEquals("Artista Desconocido", song.getArtist().getValue());
        assertEquals("Álbum Desconocido", song.getAlbum().getValue());
        assertEquals(120, song.getDuration().getSeconds());
    }

    @Test
    @DisplayName("Debe retornar null al mapear un SpotifyTrackItem nulo")
    void shouldReturnNull_whenTrackItemIsNull() {
        assertNull(mapper.toDomain(null));
    }
}
