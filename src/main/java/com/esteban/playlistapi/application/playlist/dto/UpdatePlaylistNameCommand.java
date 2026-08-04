package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-005: Actualizar Nombre de Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record UpdatePlaylistNameCommand(
        UUID playlistId,
        String newName
) {
    public UpdatePlaylistNameCommand {
        if (playlistId == null) {
            throw new InvalidCommandException("El ID de la playlist no puede ser nulo.");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new InvalidCommandException("El nuevo nombre de la playlist no puede estar vacío.");
        }
    }

    public UpdatePlaylistNameCommand(UUID playlistId, String newName, UUID userId) {
        this(playlistId, newName);
    }
}
