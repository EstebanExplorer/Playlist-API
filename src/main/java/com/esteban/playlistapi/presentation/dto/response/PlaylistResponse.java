package com.esteban.playlistapi.presentation.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * DTO HTTP Response detallado para la representación completa de una Playlist.
 * Implementado como un Record Java 21 inmutable.
 */
public record PlaylistResponse(
        UUID id,
        String name,
        UUID userId,
        int songCount,
        List<SongResponse> songs,
        String createdAt,
        String updatedAt
) {}
