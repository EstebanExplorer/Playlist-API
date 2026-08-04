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

import java.util.List;
import java.util.Objects;

/**
 * Caso de Uso UC-010: Obtener Recomendaciones Musicales mediante IA.
 * Orquesta la generación de recomendaciones musicales mediante el servicio de dominio de recomendación y el puerto de IA.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks o SDKs.
 */
public class GenerateRecommendationUseCase {

    private final PlaylistRepository playlistRepository;
    private final AiRecommendationPort aiRecommendationPort;
    private final RecommendationDomainService recommendationDomainService;

    public GenerateRecommendationUseCase(PlaylistRepository playlistRepository,
                                         AiRecommendationPort aiRecommendationPort,
                                         RecommendationDomainService recommendationDomainService) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
        this.aiRecommendationPort = Objects.requireNonNull(aiRecommendationPort, "aiRecommendationPort no puede ser nulo");
        this.recommendationDomainService = Objects.requireNonNull(recommendationDomainService, "recommendationDomainService no puede ser nulo");
    }

    public RecommendationResult execute(GenerateRecommendationCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para generar recomendaciones no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        List<Recommendation> recommendations = recommendationDomainService.getRecommendationsForPlaylist(playlist, 5);

        return RecommendationResult.fromDomain(playlist.getId(), recommendations);
    }
}
