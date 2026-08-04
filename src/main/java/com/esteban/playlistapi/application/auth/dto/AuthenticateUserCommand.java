package com.esteban.playlistapi.application.auth.dto;

/**
 * Comando de entrada para el caso de uso de Autenticación de Usuario.
 */
public record AuthenticateUserCommand(
        String username,
        String password
) {
    public AuthenticateUserCommand {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
    }
}
