package com.esteban.playlistapi.application.auth.dto;

/**
 * Resultado de salida retornado por el caso de uso de Autenticación.
 */
public record AuthenticationResult(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static AuthenticationResult bearer(String token, long expiresInSeconds) {
        return new AuthenticationResult(token, "Bearer", expiresInSeconds);
    }
}
