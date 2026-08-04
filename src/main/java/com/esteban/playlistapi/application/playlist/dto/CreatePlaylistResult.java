package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.domain.model.Playlist;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida (Result) para el caso de uso UC-002: Crear Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record CreatePlaylistResult(
        UUID id,
        String name,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CreatePlaylistResult fromDomain(Playlist playlist) {
        return new CreatePlaylistResult(
                playlist.getId(),
                playlist.getName().getValue(),
                playlist.getUserId(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
