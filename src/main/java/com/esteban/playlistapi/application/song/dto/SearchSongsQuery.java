package com.esteban.playlistapi.application.song.dto;

import com.esteban.playlistapi.shared.exception.InvalidQueryException;

/**
 * DTO de entrada (Query) para el caso de uso UC-009: Consultar Información Musical (Search Songs).
 * Implementado como un Record Java 21 inmutable.
 */
public record SearchSongsQuery(
        String query,
        int limit
) {
    public SearchSongsQuery {
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidQueryException("El término de búsqueda no puede estar vacío.");
        }
        if (limit <= 0) {
            throw new InvalidQueryException("El límite debe ser mayor que cero.");
        }
    }
}
