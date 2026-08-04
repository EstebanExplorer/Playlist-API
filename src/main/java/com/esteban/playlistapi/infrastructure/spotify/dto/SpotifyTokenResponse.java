package com.esteban.playlistapi.infrastructure.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de infraestructura para deserializar la respuesta de autenticación de Spotify (Client Credentials Flow).
 */
public record SpotifyTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresInSeconds
) {
}
