package com.esteban.playlistapi.infrastructure.recommendation.adapter;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.infrastructure.ai.adapter.GeminiRecommendationAdapter;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HybridRecommendationAdapter — Pruebas Unitarias")
class HybridRecommendationAdapterTest {

    @Mock
    private GeminiRecommendationAdapter geminiAdapter;

    @Mock
    private RuleBasedRecommendationAdapter ruleBasedAdapter;

    private HybridRecommendationAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new HybridRecommendationAdapter(geminiAdapter, ruleBasedAdapter);
    }

    // ─── Fail-Fast ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Constructor — Validaciones Fail-Fast")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si GeminiRecommendationAdapter es nulo")
        void shouldThrow_whenGeminiAdapterIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new HybridRecommendationAdapter(null, ruleBasedAdapter));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si RuleBasedRecommendationAdapter es nulo")
        void shouldThrow_whenRuleBasedAdapterIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new HybridRecommendationAdapter(geminiAdapter, null));
        }
    }

    // ─── Ruta feliz: Gemini funciona ──────────────────────────────────────────

    @Nested
    @DisplayName("generateRecommendations — Gemini disponible")
    class GeminiSuccessTests {

        @Test
        @DisplayName("Debe retornar recomendaciones de Gemini cuando responde correctamente")
        void shouldReturnGeminiResult_whenGeminiSucceeds() {
            // Arrange
            Playlist playlist = Playlist.create("Test Playlist", userId);
            List<Recommendation> geminiResult = List.of(
                    Recommendation.create("Kashmir", "Led Zeppelin", "Epic AI reason", 0.95)
            );
            when(geminiAdapter.generateRecommendations(any(Playlist.class), anyInt()))
                    .thenReturn(geminiResult);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Kashmir", result.get(0).getSongTitle().getValue());
            verify(ruleBasedAdapter, never()).generateRecommendations(any(), anyInt());
        }

        @Test
        @DisplayName("Debe NO invocar RuleBased cuando Gemini retorna recomendaciones no vacías")
        void shouldNotCallRuleBased_whenGeminiReturnsNonEmptyList() {
            // Arrange
            Playlist playlist = Playlist.create("Playlist", userId);
            when(geminiAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(List.of(Recommendation.create("Song", "Artist", "Reason", 0.9)));

            // Act
            adapter.generateRecommendations(playlist, 3);

            // Assert
            verifyNoInteractions(ruleBasedAdapter);
        }
    }

    // ─── Fallback: Gemini falla ───────────────────────────────────────────────

    @Nested
    @DisplayName("generateRecommendations — Fallback automático a RuleBased")
    class FallbackTests {

        @Test
        @DisplayName("Debe activar fallback cuando Gemini lanza ExternalServiceException")
        void shouldFallbackToRuleBased_whenGeminiThrowsExternalServiceException() {
            // Arrange
            Playlist playlist = Playlist.create("Test", userId);
            List<Recommendation> fallbackResult = List.of(
                    Recommendation.create("Fallback Song", "Fallback Artist", "Rule-based reason", 0.75)
            );
            when(geminiAdapter.generateRecommendations(any(), anyInt()))
                    .thenThrow(new ExternalServiceException("Google Gemini API", "HTTP 429 Rate Limit"));
            when(ruleBasedAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(fallbackResult);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Fallback Song", result.get(0).getSongTitle().getValue());
            verify(ruleBasedAdapter, times(1)).generateRecommendations(any(), anyInt());
        }

        @Test
        @DisplayName("Debe activar fallback cuando Gemini lanza RuntimeException genérica")
        void shouldFallbackToRuleBased_whenGeminiThrowsRuntimeException() {
            // Arrange
            Playlist playlist = Playlist.create("Test", userId);
            List<Recommendation> fallbackResult = List.of(
                    Recommendation.create("RuleBased Song", "Artist", "Reason", 0.70)
            );
            when(geminiAdapter.generateRecommendations(any(), anyInt()))
                    .thenThrow(new RuntimeException("Unexpected Gemini error"));
            when(ruleBasedAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(fallbackResult);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(ruleBasedAdapter, times(1)).generateRecommendations(any(), anyInt());
        }

        @Test
        @DisplayName("Debe activar fallback cuando Gemini retorna lista vacía")
        void shouldFallbackToRuleBased_whenGeminiReturnsEmptyList() {
            // Arrange
            Playlist playlist = Playlist.create("Test", userId);
            List<Recommendation> fallbackResult = List.of(
                    Recommendation.create("Popular Song", "Artist", "Popular reason", 0.80)
            );
            when(geminiAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(List.of());
            when(ruleBasedAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(fallbackResult);

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            assertNotNull(result);
            assertEquals("Popular Song", result.get(0).getSongTitle().getValue());
            verify(ruleBasedAdapter, times(1)).generateRecommendations(any(), anyInt());
        }

        @Test
        @DisplayName("Debe activar fallback cuando Gemini retorna null")
        void shouldFallbackToRuleBased_whenGeminiReturnsNull() {
            // Arrange
            Playlist playlist = Playlist.create("Test", userId);
            when(geminiAdapter.generateRecommendations(any(), anyInt())).thenReturn(null);
            when(ruleBasedAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(List.of(Recommendation.create("Song", "Artist", "R", 0.8)));

            // Act
            List<Recommendation> result = adapter.generateRecommendations(playlist, 5);

            // Assert
            verify(ruleBasedAdapter, times(1)).generateRecommendations(any(), anyInt());
        }

        @Test
        @DisplayName("Debe propagar correctamente el playlist y limit al adaptador de fallback")
        void shouldPassCorrectArguments_toFallbackAdapter() {
            // Arrange
            Playlist playlist = Playlist.create("My Playlist", userId);
            when(geminiAdapter.generateRecommendations(any(), anyInt()))
                    .thenThrow(new RuntimeException("timeout"));
            when(ruleBasedAdapter.generateRecommendations(any(), anyInt()))
                    .thenReturn(List.of());

            // Act
            adapter.generateRecommendations(playlist, 7);

            // Assert
            verify(ruleBasedAdapter, times(1))
                    .generateRecommendations(eq(playlist), eq(7));
        }
    }
}
