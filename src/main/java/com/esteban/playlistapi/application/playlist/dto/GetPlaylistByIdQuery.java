package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import java.util.UUID;

/**
 * DTO de entrada (Query) para el caso de uso UC-003: Consultar Playlist por ID.
 * Implementado como un Record Java 21 inmutable.
 */
public record GetPlaylistByIdQuery(
        UUID playlistId
) {
    public GetPlaylistByIdQuery {
        if (playlistId == null) {
            throw new InvalidQueryException("El ID de la playlist no puede ser nulo.");
        }
    }
}
