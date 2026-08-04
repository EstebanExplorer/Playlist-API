package com.esteban.playlistapi.application.recommendation.dto;

import com.esteban.playlistapi.domain.model.Recommendation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de salida (Result) para proyectar las recomendaciones generadas por la IA en UC-010.
 * Implementado como un Record Java 21 inmutable.
 */
public record RecommendationResult(
        UUID playlistId,
        List<String> recommendations,
        Instant generatedAt
) {
    public RecommendationResult {
        if (playlistId == null) {
            throw new IllegalArgumentException("El ID de la playlist no puede ser nulo.");
        }
        recommendations = (recommendations != null) ? List.copyOf(recommendations) : List.of();
        generatedAt = (generatedAt != null) ? generatedAt : Instant.now();
    }

    public static RecommendationResult fromDomain(UUID playlistId, List<Recommendation> domainRecommendations) {
        Objects.requireNonNull(playlistId, "playlistId no puede ser nulo");
        List<String> formattedRecommendations = (domainRecommendations != null)
                ? domainRecommendations.stream()
                        .map(r -> r.getArtistName().getValue() + " - " + r.getSongTitle().getValue())
                        .toList()
                : List.of();

        return new RecommendationResult(playlistId, formattedRecommendations, Instant.now());
    }
}
