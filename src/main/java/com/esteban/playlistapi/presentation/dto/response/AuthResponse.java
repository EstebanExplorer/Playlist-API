package com.esteban.playlistapi.presentation.dto.response;

/**
 * DTO HTTP Response para la respuesta de autenticación de usuario (UC-001).
 * Implementado como un Record Java 21 inmutable.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
    public static AuthResponse bearer(String accessToken, long expiresInSeconds) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds);
    }
}
