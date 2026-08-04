package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO HTTP Request para la remoción de una canción de una playlist (UC-008).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record RemoveSongRequest(
        @NotBlank(message = "El identificador de la canción no puede estar vacío.")
        String songId
) {}
