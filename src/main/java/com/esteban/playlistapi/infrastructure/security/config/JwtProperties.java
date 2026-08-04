package com.esteban.playlistapi.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración centralizadas para la generación y validación de tokens JWT.
 * Mapea las claves con prefijo 'jwt' desde application.yml.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b";
    private long expiration = 86400000L; // 24 horas por defecto en milisegundos
    private String issuer = "PlaylistAPI";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
