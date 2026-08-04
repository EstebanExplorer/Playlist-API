package com.esteban.playlistapi.presentation.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * DTO HTTP Response para la representación de recomendaciones de canciones impulsadas por IA (UC-010).
 * Implementado como un Record Java 21 inmutable.
 */
public record RecommendationResponse(
        UUID playlistId,
        List<String> recommendations,
        String generatedAt
) {}
