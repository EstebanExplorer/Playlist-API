package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.domain.model.Playlist;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida estándar (Result) para representar una Playlist en la capa Application.
 * Reutilizable en operaciones de lectura y escritura (Crear, Consultar por ID, Listar, Actualizar).
 * Record Java 21 inmutable.
 */
public record PlaylistResult(
        UUID id,
        String name,
        UUID userId,
        int songCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaylistResult fromDomain(Playlist playlist) {
        return new PlaylistResult(
                playlist.getId(),
                playlist.getName().getValue(),
                playlist.getUserId(),
                playlist.getSongs().size(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
