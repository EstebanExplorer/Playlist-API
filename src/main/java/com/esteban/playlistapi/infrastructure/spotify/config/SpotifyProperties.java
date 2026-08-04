package com.esteban.playlistapi.infrastructure.spotify.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración para la integración con Spotify Web API.
 * Centraliza las credenciales y URLs base.
 * Valida de forma Fail-Fast al inicio de la aplicación.
 */
@ConfigurationProperties(prefix = "spotify")
@Validated
public class SpotifyProperties {

    @NotBlank(message = "El spotify.client-id no puede estar vacío.")
    private String clientId;

    @NotBlank(message = "El spotify.client-secret no puede estar vacío.")
    private String clientSecret;

    @NotBlank(message = "El spotify.token-url no puede estar vacío.")
    private String tokenUrl;

    @NotBlank(message = "El spotify.api-url no puede estar vacío.")
    private String apiUrl;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
