package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO HTTP Request para la adición de una canción a una playlist (UC-007).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record AddSongRequest(
        @NotBlank(message = "El identificador de la canción (Spotify ID) no puede estar vacío.")
        String spotifyId
) {}
