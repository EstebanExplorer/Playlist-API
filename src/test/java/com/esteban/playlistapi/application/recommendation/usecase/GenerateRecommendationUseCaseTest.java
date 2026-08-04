package com.esteban.playlistapi.application.recommendation.usecase;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.service.RecommendationDomainService;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRecommendationUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private AiRecommendationPort aiRecommendationPort;

    @Mock
    private RecommendationDomainService recommendationDomainService;

    private GenerateRecommendationUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;
    private Recommendation mockRec1;
    private Recommendation mockRec2;

    @BeforeEach
    void setUp() {
        useCase = new GenerateRecommendationUseCase(playlistRepository, aiRecommendationPort, recommendationDomainService);
        mockPlaylist = Playlist.create("Rock Progresivo", userId);
        mockRec1 = Recommendation.create("Comfortably Numb", "Pink Floyd", "Coincide con el estilo", 0.95);
        mockRec2 = Recommendation.create("Tom Sawyer", "Rush", "Ritmo y complejidad similar", 0.90);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe generar recomendaciones musicales exitosamente cuando la playlist existe")
        void shouldGenerateRecommendationsSuccessfully_whenPlaylistExists() {
            // Arrange
            GenerateRecommendationCommand command = new GenerateRecommendationCommand(mockPlaylist.getId());

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(recommendationDomainService.getRecommendationsForPlaylist(mockPlaylist, 5))
                    .thenReturn(List.of(mockRec1, mockRec2));

            // Act
            RecommendationResult result = useCase.execute(command);

            // Assert
            assertNotNull(result);
            assertEquals(mockPlaylist.getId(), result.playlistId());
            assertEquals(2, result.recommendations().size());
            assertEquals("Pink Floyd - Comfortably Numb", result.recommendations().get(0));
            assertEquals("Rush - Tom Sawyer", result.recommendations().get(1));
            assertNotNull(result.generatedAt());

            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(recommendationDomainService, times(1)).getRecommendationsForPlaylist(mockPlaylist, 5);
            verifyNoMoreInteractions(playlistRepository, recommendationDomainService);
            verifyNoInteractions(aiRecommendationPort);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar PlaylistNotFoundException cuando la playlist no existe en el repositorio")
        void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            GenerateRecommendationCommand command = new GenerateRecommendationCommand(nonExistentId);

            when(playlistRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(nonExistentId.toString()));
            verify(playlistRepository, times(1)).findById(nonExistentId);
            verifyNoMoreInteractions(playlistRepository);
            verifyNoInteractions(recommendationDomainService, aiRecommendationPort);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException cuando el comando recibido es nulo")
        void shouldThrowInvalidCommandException_whenCommandIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("El comando para generar recomendaciones no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, recommendationDomainService, aiRecommendationPort);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con playlistId nulo")
        void shouldThrowInvalidCommandException_whenPlaylistIdIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> new GenerateRecommendationCommand(null)
            );

            assertEquals("El ID de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, recommendationDomainService, aiRecommendationPort);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si alguna dependencia del constructor es nula")
        void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new GenerateRecommendationUseCase(null, aiRecommendationPort, recommendationDomainService));
            assertThrows(NullPointerException.class, () -> new GenerateRecommendationUseCase(playlistRepository, null, recommendationDomainService));
            assertThrows(NullPointerException.class, () -> new GenerateRecommendationUseCase(playlistRepository, aiRecommendationPort, null));
        }
    }
}
