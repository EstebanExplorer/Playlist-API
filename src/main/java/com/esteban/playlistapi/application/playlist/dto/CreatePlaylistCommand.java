package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-002: Crear Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record CreatePlaylistCommand(
        String name,
        UUID userId
) {
    public CreatePlaylistCommand {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCommandException("El nombre de la playlist no puede estar vacío.");
        }
        if (userId == null) {
            throw new InvalidCommandException("El ID del usuario propietario no puede ser nulo.");
        }
    }
}
