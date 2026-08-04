package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-006: Eliminar Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record DeletePlaylistCommand(
        UUID playlistId
) {
    public DeletePlaylistCommand {
        if (playlistId == null) {
            throw new InvalidCommandException("El ID de la playlist no puede ser nulo.");
        }
    }
}
