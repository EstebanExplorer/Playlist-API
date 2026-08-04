package com.esteban.playlistapi.infrastructure.ai.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de prueba/fallback para el puerto AiRecommendationPort.
 * Pertenece a la capa Infrastructure.
 */
@Component
public class DummyAiRecommendationAdapter implements AiRecommendationPort {

    @Override
    public List<Recommendation> generateRecommendations(Playlist playlist, int limit) {
        if (playlist == null) {
            return List.of();
        }
        return List.of(
                Recommendation.create("Stairway to Heaven", "Led Zeppelin", "Similitud de genero", 0.95),
                Recommendation.create("Bohemian Rhapsody", "Queen", "Similitud de estilo", 0.90)
        );
    }
}
