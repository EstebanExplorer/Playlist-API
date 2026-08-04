package com.esteban.playlistapi.infrastructure.spotify.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyPropertiesTest {

    private Validator validator;
    private SpotifyProperties properties;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        properties = new SpotifyProperties();
        properties.setClientId("valid-client-id");
        properties.setClientSecret("valid-client-secret");
        properties.setTokenUrl("https://accounts.spotify.com/api/token");
        properties.setApiUrl("https://api.spotify.com/v1");
    }

    @Test
    @DisplayName("Debe pasar la validación cuando todas las propiedades son válidas")
    void shouldPassValidation_whenAllPropertiesAreValid() {
        Set<ConstraintViolation<SpotifyProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "No deben existir violaciones de validación");
        assertEquals("valid-client-id", properties.getClientId());
        assertEquals("valid-client-secret", properties.getClientSecret());
        assertEquals("https://accounts.spotify.com/api/token", properties.getTokenUrl());
        assertEquals("https://api.spotify.com/v1", properties.getApiUrl());
    }

    @Test
    @DisplayName("Debe fallar la validación cuando clientId está en blanco (Fail-Fast)")
    void shouldFailValidation_whenClientIdIsBlank() {
        properties.setClientId("   ");
        Set<ConstraintViolation<SpotifyProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientId")));
    }

    @Test
    @DisplayName("Debe fallar la validación cuando clientSecret es nulo o está en blanco")
    void shouldFailValidation_whenClientSecretIsBlank() {
        properties.setClientSecret("");
        Set<ConstraintViolation<SpotifyProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clientSecret")));
    }

    @Test
    @DisplayName("Debe fallar la validación cuando tokenUrl o apiUrl son nulos")
    void shouldFailValidation_whenUrlsAreNull() {
        properties.setTokenUrl(null);
        properties.setApiUrl(null);
        Set<ConstraintViolation<SpotifyProperties>> violations = validator.validate(properties);
        assertEquals(2, violations.size());
    }
}
