package com.esteban.playlistapi.presentation.dto.response;

import java.util.UUID;

/**
 * DTO HTTP Response resumido para listados de playlists de un usuario (UC-004).
 * Implementado como un Record Java 21 inmutable.
 */
public record PlaylistSummaryResponse(
        UUID id,
        String name,
        UUID userId,
        int songCount,
        String createdAt,
        String updatedAt
) {}
