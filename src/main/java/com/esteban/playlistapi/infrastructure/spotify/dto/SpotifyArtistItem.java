package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de infraestructura para deserializar los detalles de un artista en Spotify.
 */
public record SpotifyArtistItem(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name
) {
}
