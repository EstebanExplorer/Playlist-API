package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de infraestructura para representar una imagen en el catálogo de Spotify.
 */
public record SpotifyImageItem(
        @JsonProperty("url") String url,
        @JsonProperty("height") Integer height,
        @JsonProperty("width") Integer width
) {
}
