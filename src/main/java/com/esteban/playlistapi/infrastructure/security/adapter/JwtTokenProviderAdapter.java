package com.esteban.playlistapi.infrastructure.security.adapter;

import com.esteban.playlistapi.application.auth.port.TokenProviderPort;
import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.infrastructure.security.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * Adaptador de Seguridad que satisface el puerto TokenProviderPort.
 * Genera tokens de acceso JWT firmados mediante la librería JJWT (0.12.x) y algoritmo HS256.
 * Pertenece exclusivamente a la capa Infrastructure.
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final JwtProperties jwtProperties;

    public JwtTokenProviderAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = Objects.requireNonNull(jwtProperties, "jwtProperties no puede ser nulo");
    }

    @Override
    public String generateToken(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo para generar un token JWT.");
        Objects.requireNonNull(user.getId(), "El ID del usuario no puede ser nulo para generar un token JWT.");
        Objects.requireNonNull(user.getEmail(), "El email del usuario no puede ser nulo para generar un token JWT.");

        long now = System.currentTimeMillis();
        long expirationMs = jwtProperties.getExpiration();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + expirationMs);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    @Override
    public long getExpirationTimeSeconds() {
        return jwtProperties.getExpiration() / 1000L;
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            secret = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b";
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
