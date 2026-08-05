package com.esteban.playlistapi.infrastructure.ai.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.infrastructure.ai.client.GeminiClient;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRecommendationItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiRecommendationAdapter — Pruebas Unitarias")
class GeminiRecommendationAdapterTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private MusicCatalogPort musicCatalogPort;

    private GeminiRecommendationAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new GeminiRecommendationAdapter(geminiClient, musicCatalogPort);
    }

    // ─── Fail-Fast ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Constructor — Validaciones Fail-Fast")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si GeminiClient es nulo")
        void shouldThrow_whenGeminiClientIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new GeminiRecommendationAdapter(null, musicCatalogPort));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si MusicCatalogPort es nulo")
        void shouldThrow_whenMusicCatalogPortIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new GeminiRecommendationAdapter(geminiClient, null));
        }
    }

    // ─── generateRecommendations ──────────────────────────────────────────────

    @Nested
    @DisplayName("generateRecommendations — Escenarios principales")
    class GenerateRecommendationsTests {

        @Test
        @DisplayName("Debe retornar lista vacía cuando la playlist es nula")
        void shouldReturnEmptyList_whenPlaylistIsNull() {
            List<Recommendation> result = adapter.generateRecommendations(null, 5);
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verifyNoInteractions(geminiClient, musicCatalogPort);
        }

        @Test
        @DisplayName("Debe retornar recomendaciones cuando Gemini responde correctamente")
        void shouldReturnGeminiRecommendations_whenGeminiRespondsSuccessfully() {
            // Arrange
            Playlist playlist = Playlist.create("Rock Playlist", userId);
            Song existingSong = Song.create("spot-1", "Stairway to Heaven", "Led Zeppelin", "Led IV", 480);
            playlist.addSong(existingSong);

            Song candidate = Song.create("spot-2", "Kashmir", "Led Zeppelin", "Physical Graffiti", 517);
            when(musicCatalogPort.searchSongs(eq("Led Zeppelin"), anyInt()))
                    .thenReturn(List.of(candidate));
            when(musicCatalogPort.searchSongs(argThat(q -> !q.equals("Led Zeppelin")), anyInt()))
                    .thenReturn(List.of());

            List<GeminiRecommendationItem> geminiItems = List.of(
                    new GeminiRecommendationItem("Kashmir", "Led Zeppelin", "Epic riff similar to existing songs")
            );
            when(geminiClient.generateRecommendations(anyString())).thenReturn(geminiItems);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Kashmir", result.get(0).getSongTitle().getValue());
            assertEquals("Led Zeppelin", result.get(0).getArtistName().getValue());
            assertEquals("Epic riff similar to existing songs", result.get(0).getReasoning());
            verify(geminiClient, times(1)).generateRecommendations(anyString());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando Gemini retorna lista vacía")
        void shouldReturnEmptyList_whenGeminiReturnsEmpty() {
            // Arrange
            Playlist playlist = Playlist.create("Empty Playlist", userId);
            when(musicCatalogPort.searchSongs(anyString(), anyInt())).thenReturn(List.of());
            when(geminiClient.generateRecommendations(anyString())).thenReturn(List.of());

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe buscar candidatos de fallback cuando la playlist está vacía")
        void shouldUseFallbackQueries_whenPlaylistIsEmpty() {
            // Arrange
            Playlist emptyPlaylist = Playlist.create("Empty Playlist", userId);
            Song candidate = Song.create("spot-99", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354);

            when(musicCatalogPort.searchSongs(eq("Top Hits"), anyInt()))
                    .thenReturn(List.of(candidate));
            when(musicCatalogPort.searchSongs(argThat(q -> !q.equals("Top Hits")), anyInt()))
                    .thenReturn(List.of());

            List<GeminiRecommendationItem> geminiItems = List.of(
                    new GeminiRecommendationItem("Bohemian Rhapsody", "Queen", "Great fit")
            );
            when(geminiClient.generateRecommendations(anyString())).thenReturn(geminiItems);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(emptyPlaylist, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Bohemian Rhapsody", result.get(0).getSongTitle().getValue());
        }

        @Test
        @DisplayName("Debe filtrar canciones ya presentes en la playlist (por título)")
        void shouldFilterOutSongsAlreadyInPlaylist_byTitle() {
            // Arrange
            Playlist playlist = Playlist.create("My Playlist", userId);
            Song existingSong = Song.create("spot-1", "Kashmir", "Led Zeppelin", "Physical Graffiti", 517);
            playlist.addSong(existingSong);

            when(musicCatalogPort.searchSongs(anyString(), anyInt())).thenReturn(List.of());

            // Gemini sugiere una canción que ya está en la playlist
            List<GeminiRecommendationItem> geminiItems = List.of(
                    new GeminiRecommendationItem("Kashmir", "Led Zeppelin", "Already in playlist")
            );
            when(geminiClient.generateRecommendations(anyString())).thenReturn(geminiItems);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "La canción ya en la playlist debe ser filtrada.");
        }

        @Test
        @DisplayName("Debe respetar el límite máximo de recomendaciones")
        void shouldRespectLimit_whenGeminiReturnsMoreThanRequired() {
            // Arrange
            Playlist playlist = Playlist.create("Playlist", userId);
            when(musicCatalogPort.searchSongs(anyString(), anyInt())).thenReturn(List.of());

            List<GeminiRecommendationItem> manyItems = List.of(
                    new GeminiRecommendationItem("Song 1", "Artist A", "Reason 1"),
                    new GeminiRecommendationItem("Song 2", "Artist B", "Reason 2"),
                    new GeminiRecommendationItem("Song 3", "Artist C", "Reason 3"),
                    new GeminiRecommendationItem("Song 4", "Artist D", "Reason 4"),
                    new GeminiRecommendationItem("Song 5", "Artist E", "Reason 5")
            );
            when(geminiClient.generateRecommendations(anyString())).thenReturn(manyItems);

            // Act — solicitar solo 2
            List<Recommendation> result = adapter.generateRecommendations(playlist, 2);

            // Assert
            assertNotNull(result);
            assertTrue(result.size() <= 2, "No debe exceder el límite de 2 recomendaciones.");
        }

        @Test
        @DisplayName("Debe asignar razón predeterminada si Gemini omite el campo reason")
        void shouldAssignDefaultReason_whenReasonIsBlank() {
            // Arrange
            Playlist playlist = Playlist.create("Playlist", userId);
            when(musicCatalogPort.searchSongs(anyString(), anyInt())).thenReturn(List.of());

            List<GeminiRecommendationItem> items = List.of(
                    new GeminiRecommendationItem("Good Song", "Great Artist", "  ") // reason en blanco
            );
            when(geminiClient.generateRecommendations(anyString())).thenReturn(items);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertFalse(result.get(0).getReasoning().isBlank(), "Debe asignar una razón predeterminada.");
        }
    }
}
