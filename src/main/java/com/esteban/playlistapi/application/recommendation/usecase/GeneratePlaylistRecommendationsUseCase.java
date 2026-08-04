package com.esteban.playlistapi.application.recommendation.usecase;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.List;
import java.util.Objects;

/**
 * Caso de Uso para generar recomendaciones personalizadas de canciones para una playlist.
 * Orquesta la recuperación de la playlist y la delegación al puerto RecommendationEnginePort.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class GeneratePlaylistRecommendationsUseCase {

    private final PlaylistRepository playlistRepository;
    private final RecommendationEnginePort recommendationEnginePort;

    public GeneratePlaylistRecommendationsUseCase(PlaylistRepository playlistRepository,
                                                RecommendationEnginePort recommendationEnginePort) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
        this.recommendationEnginePort = Objects.requireNonNull(recommendationEnginePort, "recommendationEnginePort no puede ser nulo");
    }

    public RecommendationResult execute(GenerateRecommendationCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para generar recomendaciones no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        int limit = (command.limit() != null && command.limit() > 0) ? command.limit() : 10;
        List<Recommendation> recommendations = recommendationEnginePort.generateRecommendations(playlist, limit);

        return RecommendationResult.fromDomain(playlist.getId(), recommendations);
    }
}
