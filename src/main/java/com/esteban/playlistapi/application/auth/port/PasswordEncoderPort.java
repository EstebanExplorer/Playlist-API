package com.esteban.playlistapi.application.auth.port;

/**
 * Puerto de Salida para verificar e encriptar contraseñas de usuario.
 * Abstrae librerías como BCrypt / Spring Security.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
