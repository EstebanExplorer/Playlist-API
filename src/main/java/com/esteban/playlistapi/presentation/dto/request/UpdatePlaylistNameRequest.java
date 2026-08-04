package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO HTTP Request para la actualización del nombre de una playlist existente (UC-005).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record UpdatePlaylistNameRequest(
        @NotBlank(message = "El nuevo nombre de la playlist no puede estar vacío.")
        @Size(min = 2, max = 100, message = "El nuevo nombre de la playlist debe tener entre 2 y 100 caracteres.")
        String name
) {}
