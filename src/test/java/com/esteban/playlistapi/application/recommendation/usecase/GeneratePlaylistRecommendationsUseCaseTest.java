package com.esteban.playlistapi.application.recommendation.usecase;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratePlaylistRecommendationsUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private RecommendationEnginePort recommendationEnginePort;

    private GeneratePlaylistRecommendationsUseCase useCase;

    private final UUID playlistId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        useCase = new GeneratePlaylistRecommendationsUseCase(playlistRepository, recommendationEnginePort);
        playlist = Playlist.create("My Rock List", userId);
    }

    @Test
    @DisplayName("Debe ejecutar exitosamente la generación de recomendaciones para una playlist existente")
    void shouldExecuteSuccessfully_whenPlaylistExists() {
        // Arrange
        UUID targetPlaylistId = playlist.getId();
        GenerateRecommendationCommand command = new GenerateRecommendationCommand(targetPlaylistId, 5, userId);
        Recommendation rec = Recommendation.create("Wonderwall", "Oasis", "Basado en preferencias", 0.95);

        when(playlistRepository.findById(targetPlaylistId)).thenReturn(Optional.of(playlist));
        when(recommendationEnginePort.generateRecommendations(eq(playlist), eq(5))).thenReturn(List.of(rec));

        // Act
        RecommendationResult result = useCase.execute(command);

        // Assert
        assertNotNull(result);
        assertEquals(targetPlaylistId, result.playlistId());
        assertEquals(1, result.recommendations().size());
        assertTrue(result.recommendations().get(0).contains("Oasis - Wonderwall"));

        verify(playlistRepository, times(1)).findById(targetPlaylistId);
        verify(recommendationEnginePort, times(1)).generateRecommendations(playlist, 5);
    }

    @Test
    @DisplayName("Debe lanzar PlaylistNotFoundException cuando la playlist no existe")
    void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
        // Arrange
        GenerateRecommendationCommand command = new GenerateRecommendationCommand(playlistId, 5, userId);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlaylistNotFoundException.class, () -> useCase.execute(command));
        verify(playlistRepository, times(1)).findById(playlistId);
        verifyNoInteractions(recommendationEnginePort);
    }

    @Test
    @DisplayName("Debe lanzar InvalidCommandException cuando el comando es nulo")
    void shouldThrowInvalidCommandException_whenCommandIsNull() {
        assertThrows(InvalidCommandException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si alguna dependencia es nula (Fail-Fast)")
    void shouldThrowNullPointerException_whenDependenciesAreNull() {
        assertThrows(NullPointerException.class, () -> new GeneratePlaylistRecommendationsUseCase(null, recommendationEnginePort));
        assertThrows(NullPointerException.class, () -> new GeneratePlaylistRecommendationsUseCase(playlistRepository, null));
    }
}
