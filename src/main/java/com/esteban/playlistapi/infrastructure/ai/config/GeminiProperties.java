package com.esteban.playlistapi.infrastructure.ai.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración para la integración con Google Gemini API.
 * Sigue el patrón de {@code SpotifyProperties} y aplica Fail-Fast mediante {@code @Validated}.
 *
 * <p>Variables de entorno requeridas (Twelve-Factor App — Factor III):
 * <ul>
 *   <li>{@code GEMINI_API_KEY}     — Clave de autenticación de la API (obligatorio)</li>
 *   <li>{@code GEMINI_MODEL}       — Modelo de Gemini a utilizar (default: gemini-2.5-flash)</li>
 *   <li>{@code GEMINI_API_URL}     — URL base del endpoint REST (default: oficial de Google)</li>
 *   <li>{@code GEMINI_TIMEOUT}     — Timeout en segundos por petición (default: 15)</li>
 *   <li>{@code GEMINI_MAX_TOKENS}  — Máximo de tokens en la respuesta (default: 2048)</li>
 *   <li>{@code GEMINI_TEMPERATURE} — Creatividad del modelo, 0.0–1.0 (default: 0.3)</li>
 * </ul>
 *
 * <p>OWASP: La API Key se externaliza mediante variable de entorno.
 * Nunca debe aparecer en código fuente ni en logs.
 */
@ConfigurationProperties(prefix = "gemini")
@Validated
public class GeminiProperties {

    @NotBlank(message = "gemini.api-key es obligatorio. Configure la variable de entorno GEMINI_API_KEY.")
    private String apiKey;

    @NotBlank(message = "gemini.model es obligatorio. Configure la variable de entorno GEMINI_MODEL.")
    private String model;

    @NotBlank(message = "gemini.api-url es obligatorio. Configure la variable de entorno GEMINI_API_URL.")
    private String apiUrl;

    @Positive(message = "gemini.timeout debe ser un valor positivo en segundos.")
    private int timeout = 15;

    @Positive(message = "gemini.max-tokens debe ser un valor positivo.")
    private int maxTokens = 2048;

    @DecimalMin(value = "0.0", message = "gemini.temperature debe ser >= 0.0")
    @DecimalMax(value = "2.0", message = "gemini.temperature debe ser <= 2.0")
    private double temperature = 0.3;

    @DecimalMin(value = "0.0", message = "gemini.top-p debe ser >= 0.0")
    @DecimalMax(value = "1.0", message = "gemini.top-p debe ser <= 1.0")
    private double topP = 0.95;

    @Positive(message = "gemini.top-k debe ser un valor positivo.")
    private int topK = 40;

    @Positive(message = "gemini.max-retries debe ser un valor positivo.")
    private int maxRetries = 2;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getTopP() { return topP; }
    public void setTopP(double topP) { this.topP = topP; }

    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
