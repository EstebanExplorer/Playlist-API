package com.esteban.playlistapi.presentation.dto.response;

import java.util.UUID;

/**
 * DTO HTTP Response para la representación de canciones en respuestas REST.
 * Implementado como un Record Java 21 inmutable.
 */
public record SongResponse(
        UUID songId,
        String spotifyId,
        String title,
        String artist,
        String album,
        int durationSeconds
) {}
