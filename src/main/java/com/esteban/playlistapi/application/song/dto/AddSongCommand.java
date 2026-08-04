package com.esteban.playlistapi.application.song.dto;

import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.UUID;

/**
 * DTO de entrada (Command) para el caso de uso UC-007: Agregar Canción a Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record AddSongCommand(
        UUID playlistId,
        String songId
) {
    public AddSongCommand {
        if (playlistId == null) {
            throw new InvalidCommandException("El ID de la playlist no puede ser nulo.");
        }
        if (songId == null || songId.trim().isEmpty()) {
            throw new InvalidCommandException("El ID de la canción no puede estar vacío.");
        }
    }
}
