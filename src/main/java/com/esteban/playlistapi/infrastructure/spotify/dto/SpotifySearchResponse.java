package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de infraestructura para deserializar la respuesta del endpoint GET /v1/search de Spotify.
 */
public record SpotifySearchResponse(
        @JsonProperty("tracks") SpotifyTrackPage tracks
) {
}
