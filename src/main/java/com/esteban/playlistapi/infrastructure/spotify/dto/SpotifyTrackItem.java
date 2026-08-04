package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO de infraestructura para deserializar los detalles de una canción (track) en Spotify.
 */
public record SpotifyTrackItem(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("duration_ms") long durationMs,
        @JsonProperty("artists") List<SpotifyArtistItem> artists,
        @JsonProperty("album") SpotifyAlbumItem album
) {
}
