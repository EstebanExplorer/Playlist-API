package com.esteban.playlistapi.infrastructure.spotify.client;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.infrastructure.spotify.config.SpotifyProperties;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SpotifyClientTest {

    private SpotifyProperties properties;
    private MockRestServiceServer mockServer;
    private SpotifyClient client;

    @BeforeEach
    void setUp() {
        properties = new SpotifyProperties();
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        properties.setTokenUrl("https://accounts.spotify.com/api/token");
        properties.setApiUrl("https://api.spotify.com/v1");

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new SpotifyClient(builder, properties);
    }

    private void mockTokenEndpointSuccess() {
        String tokenJson = """
                {
                    "access_token": "mock-access-token-123",
                    "token_type": "Bearer",
                    "expires_in": 3600
                }
                """;

        mockServer.expect(requestTo("https://accounts.spotify.com/api/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic dGVzdC1jbGllbnQtaWQ6dGVzdC1jbGllbnQtc2VjcmV0"))
                .andExpect(content().string("grant_type=client_credentials"))
                .andRespond(withSuccess(tokenJson, MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Debe solicitar y retornar un Access Token válido reutilizándolo en subsecuentes llamadas")
    void shouldFetchAndCacheAccessToken() {
        // Arrange
        mockTokenEndpointSuccess();

        // Act
        String token1 = client.getOrRefreshToken();
        String token2 = client.getOrRefreshToken();

        // Assert
        assertNotNull(token1);
        assertEquals("mock-access-token-123", token1);
        assertEquals(token1, token2, "El token debe reutilizarse de la memoria caché");
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe consultar exitosamente un track por Spotify ID")
    void shouldFetchTrackByIdSuccessfully() {
        // Arrange
        mockTokenEndpointSuccess();

        String trackJson = """
                {
                    "id": "spot-001",
                    "name": "Kashmir",
                    "duration_ms": 517000,
                    "artists": [{"id": "a1", "name": "Led Zeppelin"}],
                    "album": {"id": "alb1", "name": "Physical Graffiti", "images": []}
                }
                """;

        mockServer.expect(requestTo("https://api.spotify.com/v1/tracks/spot-001"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer mock-access-token-123"))
                .andRespond(withSuccess(trackJson, MediaType.APPLICATION_JSON));

        // Act
        Optional<SpotifyTrackItem> trackOpt = client.fetchTrackById("spot-001");

        // Assert
        assertTrue(trackOpt.isPresent());
        assertEquals("spot-001", trackOpt.get().id());
        assertEquals("Kashmir", trackOpt.get().name());
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() cuando la canción no existe en Spotify (HTTP 404)")
    void shouldReturnEmptyOptional_whenTrackNotFoundHttp404() {
        // Arrange
        mockTokenEndpointSuccess();

        mockServer.expect(requestTo("https://api.spotify.com/v1/tracks/non-existent"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act
        Optional<SpotifyTrackItem> result = client.fetchTrackById("non-existent");

        // Assert
        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe realizar una búsqueda exitosa de canciones")
    void shouldSearchTracksSuccessfully() {
        // Arrange
        mockTokenEndpointSuccess();

        String searchJson = """
                {
                    "tracks": {
                        "items": [
                            {
                                "id": "spot-002",
                                "name": "Rock and Roll",
                                "duration_ms": 220000,
                                "artists": [{"id": "a1", "name": "Led Zeppelin"}],
                                "album": {"id": "alb1", "name": "Led Zeppelin IV", "images": []}
                            }
                        ],
                        "total": 1,
                        "limit": 10,
                        "offset": 0
                    }
                }
                """;

        mockServer.expect(requestTo("https://api.spotify.com/v1/search?q=Led%20Zeppelin&type=track&limit=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer mock-access-token-123"))
                .andRespond(withSuccess(searchJson, MediaType.APPLICATION_JSON));

        // Act
        List<SpotifyTrackItem> results = client.searchTracks("Led Zeppelin", 10);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Rock and Roll", results.get(0).name());
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe traducir HTTP 429 (Rate Limit Exceeded) hacia ExternalServiceException")
    void shouldThrowExternalServiceException_whenHttp429RateLimitExceeded() {
        // Arrange
        mockTokenEndpointSuccess();

        mockServer.expect(requestTo("https://api.spotify.com/v1/search?q=Too%20Many&type=track&limit=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        // Act & Assert
        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> client.searchTracks("Too Many", 5)
        );

        assertTrue(exception.getMessage().contains("Spotify API"));
        assertTrue(exception.getMessage().contains("Límite de peticiones excedido"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe retornar lista vacía al buscar con query nulo o en blanco")
    void shouldReturnEmptyList_whenSearchQueryIsBlank() {
        assertTrue(client.searchTracks(null, 10).isEmpty());
        assertTrue(client.searchTracks("   ", 10).isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException al instanciar con dependencias nulas (Fail-Fast)")
    void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
        assertThrows(NullPointerException.class, () -> new SpotifyClient(null, properties));
        assertThrows(NullPointerException.class, () -> new SpotifyClient(RestClient.builder(), null));
    }
}
