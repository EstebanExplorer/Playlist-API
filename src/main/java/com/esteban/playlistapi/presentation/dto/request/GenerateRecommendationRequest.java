package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * DTO HTTP Request para la generación de recomendaciones musicales mediante IA (UC-010).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record GenerateRecommendationRequest(
        @Positive(message = "El límite de recomendaciones debe ser un entero positivo.")
        Integer limit
) {}
