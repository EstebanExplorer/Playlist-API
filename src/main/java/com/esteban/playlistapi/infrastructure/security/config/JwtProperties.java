package com.esteban.playlistapi.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración centralizadas para la generación y validación de tokens JWT.
 * Mapea las claves con prefijo 'jwt' desde application.yml.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "PLEASE_OVERRIDE_IN_PRODUCTION_WITH_A_SECRET_KEY_OF_AT_LEAST_256_BITS_OR_32_BYTES";
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
