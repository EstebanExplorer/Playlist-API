package com.esteban.playlistapi.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Envoltorio JSON de la respuesta de recomendaciones de Gemini.
 * Corresponde exactamente al formato JSON solicitado en el prompt:
 * {"recommendations": [...]}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiRecommendationsPayload(List<GeminiRecommendationItem> recommendations) {

    public GeminiRecommendationsPayload {
        recommendations = (recommendations != null) ? List.copyOf(recommendations) : List.of();
    }

    /**
     * @return true si no hay recomendaciones en el payload
     */
    public boolean isEmpty() {
        return recommendations == null || recommendations.isEmpty();
    }
}
