package com.esteban.playlistapi.application.recommendation.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-010: Generar Recomendaciones con IA.
 * Implementado como un Record Java 21 inmutable.
 */
public record GenerateRecommendationCommand(
        UUID playlistId
) {
    public GenerateRecommendationCommand {
        if (playlistId == null) {
            throw new InvalidCommandException("El ID de la playlist no puede ser nulo.");
        }
    }
}
