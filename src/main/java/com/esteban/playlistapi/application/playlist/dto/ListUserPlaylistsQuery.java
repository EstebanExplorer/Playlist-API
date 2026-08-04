package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import java.util.UUID;

/**
 * DTO de entrada (Query) para el caso de uso UC-004: Listar Playlists por Usuario.
 * Implementado como un Record Java 21 inmutable.
 */
public record ListUserPlaylistsQuery(
        UUID userId
) {
    public ListUserPlaylistsQuery {
        if (userId == null) {
            throw new InvalidQueryException("El ID del usuario no puede ser nulo.");
        }
    }
}
