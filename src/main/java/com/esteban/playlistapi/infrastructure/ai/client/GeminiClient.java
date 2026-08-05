package com.esteban.playlistapi.infrastructure.ai.client;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.infrastructure.ai.config.GeminiProperties;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRecommendationItem;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRecommendationsPayload;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRequest;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * Cliente HTTP Enterprise para la API REST oficial de Google Gemini v1beta.
 * Implementa el endpoint {@code generateContent} usando Spring RestClient (Spring Boot 3.2).
 *
 * <p><b>Contrato de la API (verificado contra documentación oficial):</b>
 * <ul>
 *   <li>Endpoint: {@code POST {apiUrl}/{model}:generateContent?key={apiKey}}</li>
 *   <li>Headers: {@code Content-Type: application/json}, {@code Accept: application/json}</li>
 *   <li>Autenticación: query parameter {@code key} — SIN Authorization header</li>
 *   <li>GenerationConfig: temperature, maxOutputTokens, candidateCount, topP, topK, responseMimeType</li>
 * </ul>
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Construir la petición HTTP usando parámetros externalizados (GeminiProperties)</li>
 *   <li>Medir y registrar latencia por llamada</li>
 *   <li>Registrar tokens consumidos (usageMetadata) para diagnóstico</li>
 *   <li>Extraer JSON de la respuesta con limpieza robusta de Markdown</li>
 *   <li>Manejar exhaustivamente todos los códigos HTTP de error</li>
 *   <li>Traducir excepciones de infraestructura a {@link ExternalServiceException}</li>
 * </ul>
 *
 * <p><b>Seguridad (OWASP):</b>
 * <ul>
 *   <li>La API Key NUNCA aparece en logs — solo se incluye en la URL (no loggeada)</li>
 *   <li>Los prompts completos y respuestas completas NO se registran en logs</li>
 *   <li>Los cuerpos de error de Gemini se truncan a 300 caracteres</li>
 * </ul>
 *
 * <p>El timeout se configura externamente en {@code GeminiConfiguration} mediante
 * {@code SimpleClientHttpRequestFactory} — este cliente solo construye el RestClient.
 */
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String SERVICE_NAME = "Google Gemini API";

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Constructor de producción. El timeout se configura externamente en
     * {@code GeminiConfiguration} antes de que el {@code RestClient.Builder}
     * sea pasado a este constructor.
     */
    public GeminiClient(RestClient.Builder restClientBuilder,
                        GeminiProperties properties,
                        ObjectMapper objectMapper) {
        this.properties = Objects.requireNonNull(properties, "GeminiProperties no puede ser nulo.");
        Objects.requireNonNull(restClientBuilder, "RestClient.Builder no puede ser nulo.");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper no puede ser nulo.");
        this.restClient = restClientBuilder.build();
    }

    /**
     * Envía un prompt a Gemini y retorna las recomendaciones parseadas.
     *
     * <p>Si Gemini responde con JSON malformado, lista vacía, o cualquier error de
     * red o HTTP, lanza {@link ExternalServiceException} que el
     * {@code HybridRecommendationAdapter} captura para activar el fallback automático.
     *
     * <p>Logs generados (sin exponer secretos):
     * <ul>
     *   <li>INFO: modelo, longitud del prompt, cantidad de candidatos en respuesta</li>
     *   <li>INFO: latencia en ms, tokens consumidos (si disponibles)</li>
     *   <li>WARN: errores HTTP con body truncado (máx 300 chars)</li>
     *   <li>ERROR: timeouts y errores de red</li>
     * </ul>
     *
     * @param prompt el prompt completo con contexto de playlist y candidatos de Spotify
     * @return lista de recomendaciones válidas, puede ser vacía (nunca null)
     * @throws ExternalServiceException en caso de error HTTP, timeout o red
     */
    public List<GeminiRecommendationItem> generateRecommendations(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            log.warn("[Gemini] Prompt nulo o vacío recibido. Retornando lista vacía.");
            return List.of();
        }

        String endpointUrl = buildEndpointUrl();
        GeminiRequest request = buildRequest(prompt);

        log.info("[Gemini] Invocando API → modelo: '{}', prompt: {} chars, maxTokens: {}, temperature: {}",
                properties.getModel(), prompt.length(), properties.getMaxTokens(), properties.getTemperature());

        long startTime = System.currentTimeMillis();

        try {
            GeminiResponse response = restClient.post()
                    .uri(endpointUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            long elapsedMs = System.currentTimeMillis() - startTime;
            return parseAndLogResponse(response, elapsedMs);

        } catch (HttpClientErrorException e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.warn("[Gemini] Error HTTP {} en {}ms → modelo: '{}'",
                    e.getStatusCode().value(), elapsedMs, properties.getModel());
            handleClientError(e);
            return List.of(); // Unreachable — handleClientError always throws
        } catch (HttpServerErrorException e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.warn("[Gemini] Error servidor HTTP {} en {}ms → modelo: '{}'",
                    e.getStatusCode().value(), elapsedMs, properties.getModel());
            handleServerError(e);
            return List.of(); // Unreachable — handleServerError always throws
        } catch (ResourceAccessException e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.error("[Gemini] Timeout o error de red en {}ms → causa: {}", elapsedMs, e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "Timeout o problema de red al contactar Google Gemini API (timeout configurado: "
                            + properties.getTimeout() + "s).", e);
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Gemini] Error inesperado al invocar la API: {}", e.getMessage());
            throw new ExternalServiceException(SERVICE_NAME,
                    "Error inesperado al invocar Google Gemini API: " + e.getMessage(), e);
        }
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    /**
     * Construye la URL del endpoint con modelo y API Key.
     * OWASP: Esta URL nunca se loggea para proteger la API Key.
     */
    private String buildEndpointUrl() {
        return properties.getApiUrl().trim()
                + "/" + properties.getModel().trim()
                + ":generateContent?key=" + properties.getApiKey().trim();
    }

    /**
     * Construye el payload de la petición usando todos los parámetros de generación
     * externalizados en {@link GeminiProperties}.
     */
    private GeminiRequest buildRequest(String prompt) {
        return GeminiRequest.of(
                prompt,
                properties.getMaxTokens(),
                properties.getTemperature(),
                properties.getTopP(),
                properties.getTopK()
        );
    }

    /**
     * Parsea la respuesta de Gemini, registra latencia y tokens consumidos.
     */
    private List<GeminiRecommendationItem> parseAndLogResponse(GeminiResponse response, long elapsedMs) {
        if (response == null) {
            log.warn("[Gemini] Respuesta HTTP nula recibida en {}ms.", elapsedMs);
            return List.of();
        }

        // Log de tokens consumidos (si disponibles en usageMetadata)
        if (response.usageMetadata() != null) {
            log.info("[Gemini] Llamada exitosa → {}ms | tokens: [{}]",
                    elapsedMs, response.usageMetadata().toLogString());
        } else {
            log.info("[Gemini] Llamada exitosa → {}ms | tokens: no reportados.", elapsedMs);
        }

        String rawText = response.extractFirstCandidateText();
        if (rawText.isBlank()) {
            log.warn("[Gemini] Texto de respuesta vacío en el primer candidato.");
            return List.of();
        }

        return parseRecommendationsJson(rawText);
    }

    /**
     * Parsea el texto JSON de recomendaciones con limpieza robusta de Markdown.
     *
     * <p>Estrategia de extracción (en orden):
     * <ol>
     *   <li>Elimina bloques de código Markdown si existen (```json ... ```)</li>
     *   <li>Busca las fronteras del objeto JSON ({...}) para manejar texto pre/post</li>
     *   <li>Parsea el JSON limpio con Jackson</li>
     *   <li>Filtra ítems inválidos (sin título o artista)</li>
     * </ol>
     *
     * <p>Método package-private para facilitar tests unitarios.
     */
    List<GeminiRecommendationItem> parseRecommendationsJson(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return List.of();
        }
        try {
            String cleaned = extractJson(jsonText);
            if (cleaned.isBlank()) {
                log.warn("[Gemini] No se encontró JSON válido en la respuesta del modelo.");
                return List.of();
            }

            GeminiRecommendationsPayload payload =
                    objectMapper.readValue(cleaned, GeminiRecommendationsPayload.class);

            if (payload == null || payload.isEmpty()) {
                log.info("[Gemini] El payload de recomendaciones está vacío tras el parseo.");
                return List.of();
            }

            List<GeminiRecommendationItem> valid = payload.recommendations().stream()
                    .filter(GeminiRecommendationItem::isValid)
                    .toList();

            log.info("[Gemini] {} recomendaciones válidas extraídas ({} filtradas por inválidas).",
                    valid.size(), payload.recommendations().size() - valid.size());
            return valid;

        } catch (Exception e) {
            log.warn("[Gemini] No se pudo parsear JSON de respuesta. Causa: {}. Preview (200 chars): '{}'",
                    e.getMessage(),
                    jsonText.length() > 200 ? jsonText.substring(0, 200) + "..." : jsonText);
            return List.of();
        }
    }

    /**
     * Extrae el objeto JSON de una cadena que puede contener texto libre o Markdown.
     *
     * <p>Paso 1: Elimina delimitadores de código Markdown (```json, ```).
     * <p>Paso 2: Busca las fronteras del primer objeto JSON válido ({...}).
     * <p>Paso 3: Retorna la subcadena entre la primera '{' y la última '}'.
     */
    private String extractJson(String text) {
        String working = text.trim();

        // Paso 1: limpiar bloques Markdown
        if (working.contains("```")) {
            int codeStart = working.indexOf("```");
            int codeEnd = working.lastIndexOf("```");
            if (codeStart < codeEnd) {
                String inner = working.substring(codeStart + 3, codeEnd);
                // Eliminar identificador de lenguaje (json, JSON, etc.)
                int newline = inner.indexOf('\n');
                if (newline >= 0 && inner.substring(0, newline).trim().matches("[a-zA-Z]*")) {
                    inner = inner.substring(newline + 1);
                }
                working = inner.trim();
            }
        }

        // Paso 2: extraer por fronteras JSON
        int jsonStart = working.indexOf('{');
        int jsonEnd = working.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return working.substring(jsonStart, jsonEnd + 1).trim();
        }

        return working.trim();
    }

    /**
     * Maneja todos los errores HTTP 4xx de la API de Gemini.
     * SIEMPRE lanza ExternalServiceException para que HybridAdapter active el fallback.
     */
    private void handleClientError(HttpClientErrorException e) {
        HttpStatusCode status = e.getStatusCode();
        String body = truncate(e.getResponseBodyAsString(), 300);

        if (status == HttpStatusCode.valueOf(400)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Solicitud inválida rechazada por Gemini (HTTP 400). Revisar formato del prompt o configuración. Body: " + body, e);
        }
        if (status == HttpStatusCode.valueOf(401)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "API Key de Gemini no autorizada (HTTP 401). Verifique GEMINI_API_KEY.", e);
        }
        if (status == HttpStatusCode.valueOf(403)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Acceso prohibido a Gemini (HTTP 403). Verifique permisos de la API Key.", e);
        }
        if (status == HttpStatusCode.valueOf(404)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Endpoint o modelo de Gemini no encontrado (HTTP 404). Verifique GEMINI_MODEL y GEMINI_API_URL.", e);
        }
        if (status == HttpStatusCode.valueOf(408)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Timeout de solicitud en Gemini (HTTP 408). El modelo tardó demasiado.", e);
        }
        if (status == HttpStatusCode.valueOf(429)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Rate limit o cuota de Gemini excedida (HTTP 429). Fallback activado.", e);
        }
        throw new ExternalServiceException(SERVICE_NAME,
                "Error HTTP " + status.value() + " al invocar Gemini API. Body: " + body, e);
    }

    /**
     * Maneja errores HTTP 5xx de la API de Gemini.
     * SIEMPRE lanza ExternalServiceException.
     */
    private void handleServerError(HttpServerErrorException e) {
        String body = truncate(e.getResponseBodyAsString(), 300);
        if (e.getStatusCode() == HttpStatusCode.valueOf(502)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Bad Gateway desde Gemini (HTTP 502). Servicio intermedio con problemas.", e);
        }
        if (e.getStatusCode() == HttpStatusCode.valueOf(503)) {
            throw new ExternalServiceException(SERVICE_NAME,
                    "Servicio Gemini no disponible (HTTP 503). Sobrecarga o mantenimiento.", e);
        }
        throw new ExternalServiceException(SERVICE_NAME,
                "Error de servidor en Google Gemini API (HTTP " + e.getStatusCode().value()
                        + "). Body: " + body, e);
    }

    /** Trunca una cadena para logging seguro. */
    private String truncate(String text, int maxLength) {
        if (text == null) return "(null)";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
