package com.esteban.playlistapi.domain.service;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;

import java.util.List;

/**
 * Servicio del Dominio para orquestar la solicitud de recomendaciones
 * garantizando validaciones de negocio antes de invocar el puerto de IA.
 */
public class RecommendationDomainService {

    private final AiRecommendationPort aiRecommendationPort;

    public RecommendationDomainService(AiRecommendationPort aiRecommendationPort) {
        if (aiRecommendationPort == null) {
            throw new IllegalArgumentException("El puerto de recomendación IA no puede ser nulo.");
        }
        this.aiRecommendationPort = aiRecommendationPort;
    }

    public List<Recommendation> getRecommendationsForPlaylist(Playlist playlist, int limit) {
        if (playlist == null) {
            throw new IllegalArgumentException("La playlist no puede ser nula para generar recomendaciones.");
        }
        int validLimit = (limit <= 0) ? 5 : Math.min(limit, 20); // Regla de negocio: entre 1 y 20
        return aiRecommendationPort.generateRecommendations(playlist, validLimit);
    }
}
