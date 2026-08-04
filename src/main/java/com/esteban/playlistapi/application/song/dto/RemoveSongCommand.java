package com.esteban.playlistapi.application.song.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-008: Remover Canción de Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record RemoveSongCommand(
        UUID playlistId,
        UUID songId
) {
    public RemoveSongCommand {
        if (playlistId == null) {
            throw new InvalidCommandException("El ID de la playlist no puede ser nulo.");
        }
        if (songId == null) {
            throw new InvalidCommandException("El ID de la canción no puede ser nulo.");
        }
    }

    public RemoveSongCommand(UUID playlistId, UUID songId, UUID userId) {
        this(playlistId, songId);
    }
}
