package com.esteban.playlistapi.infrastructure.security.adapter;

import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.infrastructure.security.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderAdapterTest {

    private JwtProperties jwtProperties;
    private JwtTokenProviderAdapter adapter;

    private final String secret = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b";
    private final long expirationMs = 86400000L;
    private final String issuer = "PlaylistAPI";

    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secret);
        jwtProperties.setExpiration(expirationMs);
        jwtProperties.setIssuer(issuer);

        adapter = new JwtTokenProviderAdapter(jwtProperties);
        mockUser = User.create("Esteban", "esteban@example.com", "HashedPassword123!");
    }

    @Nested
    @DisplayName("Pruebas de Generación de Token (generateToken)")
    class GenerateTokenTests {

        @Test
        @DisplayName("Debe generar un token JWT válido con Subject, Issuer, Claims y firma correcta")
        void shouldGenerateValidJwtTokenForUser() {
            // Act
            String token = adapter.generateToken(mockUser);

            // Assert
            assertNotNull(token);
            assertFalse(token.isBlank());

            // Verificación y parseo del JWT generado usando JJWT 0.12.x
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertEquals(mockUser.getId().toString(), claims.getSubject());
            assertEquals("esteban@example.com", claims.get("email", String.class));
            assertEquals(issuer, claims.getIssuer());
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiration());
            assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al intentar generar un token para un usuario nulo (Fail-Fast)")
        void shouldThrowNullPointerException_whenUserIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.generateToken(null)
            );
            assertEquals("El usuario no puede ser nulo para generar un token JWT.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Pruebas de Tiempo de Expiración (getExpirationTimeSeconds)")
    class ExpirationTests {

        @Test
        @DisplayName("Debe retornar la cantidad correcta de segundos de expiración")
        void shouldReturnExpirationTimeInSeconds() {
            // Act
            long expirationSeconds = adapter.getExpirationTimeSeconds();

            // Assert
            assertEquals(86400L, expirationSeconds); // 86400000 ms / 1000 = 86400 s
        }
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si las propiedades de configuración son nulas")
    void shouldThrowNullPointerException_whenJwtPropertiesIsNull() {
        assertThrows(NullPointerException.class, () -> new JwtTokenProviderAdapter(null));
    }
}
