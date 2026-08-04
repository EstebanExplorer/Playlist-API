package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO de infraestructura para deserializar el objeto de paginación de canciones de Spotify.
 */
public record SpotifyTrackPage(
        @JsonProperty("items") List<SpotifyTrackItem> items,
        @JsonProperty("total") Integer total,
        @JsonProperty("limit") Integer limit,
        @JsonProperty("offset") Integer offset
) {
}
