package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO HTTP Request para la búsqueda de canciones en el catálogo musical (UC-009).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record SearchSongsRequest(
        @NotBlank(message = "El término de búsqueda no puede estar vacío.")
        String query,

        @Positive(message = "El límite de resultados debe ser un entero positivo mayor a cero.")
        Integer limit
) {}
