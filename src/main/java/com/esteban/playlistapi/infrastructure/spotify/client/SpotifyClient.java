package com.esteban.playlistapi.infrastructure.spotify.client;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.infrastructure.spotify.config.SpotifyProperties;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifySearchResponse;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTokenResponse;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cliente HTTP para la Spotify Web API utilizando RestClient de Spring Boot 3.2.
 * Implementa el Client Credentials Flow con caché de token thread-safe y
 * traducción de excepciones HTTP a ExternalServiceException.
 */
public class SpotifyClient {

    private static final Logger log = LoggerFactory.getLogger(SpotifyClient.class);
    private static final String SERVICE_NAME = "Spotify API";
    private static final long REFRESH_BUFFER_SECONDS = 60;

    private final RestClient restClient;
    private final SpotifyProperties properties;
    private final AtomicReference<CachedToken> cachedTokenRef = new AtomicReference<>();

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isValid() {
            return accessToken != null && Instant.now().plusSeconds(REFRESH_BUFFER_SECONDS).isBefore(expiresAt);
        }
    }

    public SpotifyClient(RestClient.Builder restClientBuilder, SpotifyProperties properties) {
        this.properties = Objects.requireNonNull(properties, "SpotifyProperties no puede ser nulo.");
        Objects.requireNonNull(restClientBuilder, "RestClient.Builder no puede ser nulo.");
        this.restClient = restClientBuilder.build();
    }

    /**
     * Obtiene el token de acceso actual o renueva si ha expirado (thread-safe).
     */
    public String getOrRefreshToken() {
        CachedToken current = cachedTokenRef.get();
        if (current != null && current.isValid()) {
            return current.accessToken();
        }

        synchronized (this) {
            current = cachedTokenRef.get();
            if (current != null && current.isValid()) {
                return current.accessToken();
            }

            log.info("Iniciando solicitud de Access Token a Spotify (Client Credentials Flow)...");
            SpotifyTokenResponse tokenResponse = requestNewAccessToken();

            long expiresIn = tokenResponse.expiresInSeconds() > 0 ? tokenResponse.expiresInSeconds() : 3600;
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);
            CachedToken newToken = new CachedToken(tokenResponse.accessToken(), expiresAt);
            cachedTokenRef.set(newToken);

            log.info("Access Token obtenido exitosamente de Spotify. Expira en {} segundos.", expiresIn);
            return newToken.accessToken();
        }
    }

    private SpotifyTokenResponse requestNewAccessToken() {
        try {
            String credentials = properties.getClientId() + ":" + properties.getClientSecret();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");

            SpotifyTokenResponse response = restClient.post()
                    .uri(properties.getTokenUrl())
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(SpotifyTokenResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new ExternalServiceException(SERVICE_NAME, "Respuesta de token vacía o inválida recibida de Spotify.");
            }

            return response;
        } catch (HttpClientErrorException e) {
            log.error("Error HTTP {} al solicitar token a Spotify.", e.getStatusCode().value());
            throw handleHttpException(e, "solicitar token");
        } catch (HttpServerErrorException e) {
            log.error("Error servidor Spotify {} al solicitar token.", e.getStatusCode().value());
            throw handleHttpException(e, "solicitar token");
        } catch (ResourceAccessException e) {
            log.error("Timeout o error de red al conectar con Spotify Token Endpoint.");
            throw new ExternalServiceException(SERVICE_NAME, "Timeout o problema de red al conectar con el servidor de autenticación de Spotify.", e);
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al solicitar token a Spotify.");
            throw new ExternalServiceException(SERVICE_NAME, "Error no esperado durante la autenticación con Spotify: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta una canción individual por su ID de Spotify.
     */
    public Optional<SpotifyTrackItem> fetchTrackById(String spotifyId) {
        if (spotifyId == null || spotifyId.isBlank()) {
            return Optional.empty();
        }

        String token = getOrRefreshToken();
        String url = properties.getApiUrl() + "/tracks/" + spotifyId.trim();

        log.debug("Iniciando consulta de track id: {} a Spotify...", spotifyId);
        try {
            SpotifyTrackItem track = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SpotifyTrackItem.class);

            log.debug("Track id: {} obtenido exitosamente de Spotify.", spotifyId);
            return Optional.ofNullable(track);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(404)) {
                log.info("Track id: {} no encontrado en Spotify (404).", spotifyId);
                return Optional.empty();
            }
            if (e.getStatusCode() == HttpStatusCode.valueOf(401)) {
                log.warn("Token expirado o rechazado (401). Forzando renovación e reintentando...");
                cachedTokenRef.set(null);
                return retryFetchTrackById(spotifyId);
            }
            throw handleHttpException(e, "consultar track por ID");
        } catch (HttpServerErrorException e) {
            throw handleHttpException(e, "consultar track por ID");
        } catch (ResourceAccessException e) {
            log.error("Timeout o error de red al consultar track id: {}.", spotifyId);
            throw new ExternalServiceException(SERVICE_NAME, "Timeout o problema de red al consultar el catálogo de Spotify.", e);
        } catch (Exception e) {
            log.error("Error inesperado al consultar track id: {}.", spotifyId);
            throw new ExternalServiceException(SERVICE_NAME, "Error al consultar la canción en Spotify: " + e.getMessage(), e);
        }
    }

    private Optional<SpotifyTrackItem> retryFetchTrackById(String spotifyId) {
        String token = getOrRefreshToken();
        String url = properties.getApiUrl() + "/tracks/" + spotifyId.trim();
        try {
            SpotifyTrackItem track = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SpotifyTrackItem.class);
            return Optional.ofNullable(track);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(404)) {
                return Optional.empty();
            }
            throw handleHttpException(e, "reintentar consultar track por ID");
        } catch (Exception e) {
            throw new ExternalServiceException(SERVICE_NAME, "Error al reintentar consultar la canción en Spotify: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza una búsqueda de canciones en el catálogo de Spotify.
     */
    public List<SpotifyTrackItem> searchTracks(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int validLimit = Math.max(1, Math.min(limit, 50));
        String token = getOrRefreshToken();

        log.debug("Iniciando búsqueda en Spotify query: '{}', limit: {}...", query, validLimit);
        try {
            SpotifySearchResponse response = restClient.get()
                    .uri(properties.getApiUrl() + "/search?q={query}&type=track&limit={limit}", query.trim(), validLimit)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SpotifySearchResponse.class);

            if (response == null || response.tracks() == null || response.tracks().items() == null) {
                log.debug("Búsqueda en Spotify devolvió resultado vacío.");
                return List.of();
            }

            log.debug("Búsqueda en Spotify exitosa. {} canciones encontradas.", response.tracks().items().size());
            return response.tracks().items();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(401)) {
                log.warn("Token expirado o rechazado (401) durante búsqueda. Forzando renovación y reintento...");
                cachedTokenRef.set(null);
                return retrySearchTracks(query, validLimit);
            }
            throw handleHttpException(e, "buscar canciones");
        } catch (HttpServerErrorException e) {
            throw handleHttpException(e, "buscar canciones");
        } catch (ResourceAccessException e) {
            log.error("Timeout o error de red al buscar canciones en Spotify.");
            throw new ExternalServiceException(SERVICE_NAME, "Timeout o problema de red al buscar en el catálogo de Spotify.", e);
        } catch (Exception e) {
            log.error("Error inesperado al buscar canciones en Spotify.");
            throw new ExternalServiceException(SERVICE_NAME, "Error al realizar la búsqueda en Spotify: " + e.getMessage(), e);
        }
    }

    private List<SpotifyTrackItem> retrySearchTracks(String query, int limit) {
        String token = getOrRefreshToken();
        try {
            SpotifySearchResponse response = restClient.get()
                    .uri(properties.getApiUrl() + "/search?q={query}&type=track&limit={limit}", query.trim(), limit)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SpotifySearchResponse.class);

            if (response == null || response.tracks() == null || response.tracks().items() == null) {
                return List.of();
            }
            return response.tracks().items();
        } catch (HttpClientErrorException e) {
            throw handleHttpException(e, "reintentar buscar canciones");
        } catch (Exception e) {
            throw new ExternalServiceException(SERVICE_NAME, "Error al reintentar la búsqueda en Spotify: " + e.getMessage(), e);
        }
    }

    private ExternalServiceException handleHttpException(HttpClientErrorException e, String action) {
        HttpStatusCode status = e.getStatusCode();
        if (status == HttpStatusCode.valueOf(401)) {
            return new ExternalServiceException(SERVICE_NAME, "Credenciales no autorizadas (HTTP 401) al " + action + ".", e);
        } else if (status == HttpStatusCode.valueOf(403)) {
            return new ExternalServiceException(SERVICE_NAME, "Acceso prohibido por Spotify (HTTP 403) al " + action + ".", e);
        } else if (status == HttpStatusCode.valueOf(404)) {
            return new ExternalServiceException(SERVICE_NAME, "Recurso no encontrado en Spotify (HTTP 404) al " + action + ".", e);
        } else if (status == HttpStatusCode.valueOf(429)) {
            return new ExternalServiceException(SERVICE_NAME, "Límite de peticiones excedido en Spotify (HTTP 429 - Rate Limit) al " + action + ".", e);
        }
        return new ExternalServiceException(SERVICE_NAME, "Error HTTP " + status.value() + " devuelto por Spotify al " + action + ".", e);
    }

    private ExternalServiceException handleHttpException(HttpServerErrorException e, String action) {
        return new ExternalServiceException(SERVICE_NAME, "Error de servidor en Spotify (HTTP " + e.getStatusCode().value() + ") al " + action + ".", e);
    }
}
