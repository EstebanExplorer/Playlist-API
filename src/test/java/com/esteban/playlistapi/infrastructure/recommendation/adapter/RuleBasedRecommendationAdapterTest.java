package com.esteban.playlistapi.infrastructure.recommendation.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleBasedRecommendationAdapterTest {

    @Mock
    private MusicCatalogPort musicCatalogPort;

    private RuleBasedRecommendationAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new RuleBasedRecommendationAdapter(musicCatalogPort);
    }

    @Test
    @DisplayName("Debe retornar recomendaciones basadas en artistas predominantes de la playlist")
    void shouldGenerateRecommendationsBasedOnPredominantArtists() {
        // Arrange
        Playlist playlist = Playlist.create("My Rock Playlist", userId);
        Song existingSong1 = Song.create("spot-1", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482);
        Song existingSong2 = Song.create("spot-2", "Kashmir", "Led Zeppelin", "Physical Graffiti", 517);
        playlist.addSong(existingSong1);
        playlist.addSong(existingSong2);

        Song candidateSong1 = Song.create("spot-3", "Black Dog", "Led Zeppelin", "Led Zeppelin IV", 296);
        Song candidateSong2 = Song.create("spot-4", "Immigrant Song", "Led Zeppelin", "Led Zeppelin III", 146);

        when(musicCatalogPort.searchSongs("Led Zeppelin", 10)).thenReturn(List.of(candidateSong1, candidateSong2));

        // Act
        List<Recommendation> recommendations = adapter.generateRecommendations(playlist, 5);

        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().anyMatch(r -> r.getSongTitle().getValue().equals("Black Dog")));
        verify(musicCatalogPort, times(1)).searchSongs("Led Zeppelin", 10);
    }

    @Test
    @DisplayName("Debe utilizar búsquedas populares de fallback cuando la playlist no tiene canciones")
    void shouldFallbackToPopularQueries_whenPlaylistIsEmpty() {
        // Arrange
        Playlist emptyPlaylist = Playlist.create("Empty Playlist", userId);
        Song candidateSong = Song.create("spot-99", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354);

        when(musicCatalogPort.searchSongs("Top Hits", 10)).thenReturn(List.of(candidateSong));
        when(musicCatalogPort.searchSongs(argThat(query -> !query.equals("Top Hits")), anyInt())).thenReturn(List.of());

        // Act
        List<Recommendation> recommendations = adapter.generateRecommendations(emptyPlaylist, 5);

        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals("Bohemian Rhapsody", recommendations.get(0).getSongTitle().getValue());
    }

    @Test
    @DisplayName("Debe retornar lista vacía si la playlist es nula")
    void shouldReturnEmptyList_whenPlaylistIsNull() {
        List<Recommendation> recommendations = adapter.generateRecommendations(null, 5);
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si el puerto MusicCatalogPort es nulo (Fail-Fast)")
    void shouldThrowNullPointerException_whenMusicCatalogPortIsNull() {
        assertThrows(NullPointerException.class, () -> new RuleBasedRecommendationAdapter(null));
    }
}
