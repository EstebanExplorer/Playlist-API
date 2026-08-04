package com.esteban.playlistapi.application.auth.port;

import com.esteban.playlistapi.domain.model.User;

/**
 * Puerto de Salida para la generación y validación de tokens de acceso JWT.
 * Abstrae librerías como JJWT o proveedores OAuth.
 */
public interface TokenProviderPort {

    String generateToken(User user);

    long getExpirationTimeSeconds();
}
