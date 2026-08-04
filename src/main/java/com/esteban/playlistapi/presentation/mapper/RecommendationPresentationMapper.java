package com.esteban.playlistapi.presentation.mapper;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.presentation.dto.request.GenerateRecommendationRequest;
import com.esteban.playlistapi.presentation.dto.response.RecommendationResponse;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Mapper de presentación para el flujo de recomendaciones con IA (UC-010).
 * Traduce entre los DTOs HTTP (Presentation) y los DTOs internos (Application).
 * Pertenece exclusivamente a la capa Presentation.
 */
@Component
public class RecommendationPresentationMapper {

    public GenerateRecommendationCommand toCommand(UUID playlistId, GenerateRecommendationRequest request, UUID userId) {
        Objects.requireNonNull(playlistId, "playlistId no puede ser nulo");
        Objects.requireNonNull(userId, "userId no puede ser nulo");

        Integer limit = (request != null && request.limit() != null) ? request.limit() : 5;
        return new GenerateRecommendationCommand(playlistId, limit, userId);
    }

    public RecommendationResponse toResponse(RecommendationResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");

        return new RecommendationResponse(
                result.playlistId(),
                result.recommendations(),
                result.generatedAt() != null ? result.generatedAt().toString() : null
        );
    }
}
