package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO HTTP Request para la creación de una nueva playlist (UC-002).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record CreatePlaylistRequest(
        @NotBlank(message = "El nombre de la playlist no puede estar vacío.")
        @Size(min = 2, max = 100, message = "El nombre de la playlist debe tener entre 2 y 100 caracteres.")
        String name
) {}
