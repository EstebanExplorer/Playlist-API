package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO de infraestructura para deserializar los detalles de un álbum en Spotify.
 */
public record SpotifyAlbumItem(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("images") List<SpotifyImageItem> images
) {
}
