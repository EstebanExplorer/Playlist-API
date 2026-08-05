package com.esteban.playlistapi.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que representa un ítem de recomendación musical devuelto por Gemini.
 * Modela exactamente la estructura JSON que se solicita a la IA en el prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiRecommendationItem(
        String title,
        String artist,
        String reason
) {

    /**
     * Verifica si este ítem contiene datos mínimos válidos para ser procesado.
     *
     * @return true si tiene título y artista no nulos y no vacíos
     */
    public boolean isValid() {
        return title != null && !title.isBlank()
                && artist != null && !artist.isBlank();
    }
}
