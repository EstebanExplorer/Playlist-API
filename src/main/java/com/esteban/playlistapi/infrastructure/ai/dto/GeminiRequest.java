package com.esteban.playlistapi.infrastructure.ai.dto;

import java.util.List;

/**
 * DTO de solicitud para la API de Google Gemini (generateContent endpoint).
 * Modela exactamente la estructura JSON requerida por la API REST oficial v1beta de Gemini.
 *
 * <p>Endpoint: {@code POST {apiUrl}/{model}:generateContent?key={apiKey}}
 * <p>Headers: {@code Content-Type: application/json}
 * <p>Sin {@code Authorization} header — la autenticación va como query param {@code key}.
 *
 * @see <a href="https://ai.google.dev/api/generate-content">Gemini API — generateContent</a>
 */
public record GeminiRequest(
        List<GeminiContent> contents,
        GeminiGenerationConfig generationConfig
) {

    /**
     * Construye una solicitud de texto completa usando todos los parámetros de generación.
     *
     * @param prompt       el prompt completo con contexto de playlist y candidatos
     * @param maxTokens    límite de tokens en la respuesta
     * @param temperature  creatividad del modelo (0.0 = determinista, 1.0 = creativo)
     * @param topP         nucleus sampling threshold (0.0–1.0)
     * @param topK         top-k sampling (número de tokens candidatos)
     * @return instancia de GeminiRequest lista para serializar y enviar vía RestClient
     */
    public static GeminiRequest of(String prompt,
                                   int maxTokens,
                                   double temperature,
                                   double topP,
                                   int topK) {
        GeminiPart part = new GeminiPart(prompt);
        GeminiContent content = new GeminiContent(List.of(part));
        GeminiGenerationConfig config = new GeminiGenerationConfig(
                temperature, maxTokens, 1, topP, topK, "application/json"
        );
        return new GeminiRequest(List.of(content), config);
    }

    /** Representa un turno de conversación con la IA (role=user implícito). */
    public record GeminiContent(List<GeminiPart> parts) {}

    /** Una parte de texto dentro del turno. */
    public record GeminiPart(String text) {}

    /**
     * Parámetros de generación enviados a la API de Gemini.
     * Todos los campos corresponden exactamente a las claves JSON de la API oficial.
     *
     * @param temperature      0.0–2.0 — controla creatividad vs determinismo
     * @param maxOutputTokens  límite de tokens en la respuesta
     * @param candidateCount   número de candidatos a generar (siempre 1 para recomendaciones)
     * @param topP             nucleus sampling probability (0.0–1.0)
     * @param topK             top-k candidates filter
     * @param responseMimeType fuerza JSON en la respuesta del modelo
     */
    public record GeminiGenerationConfig(
            double temperature,
            int maxOutputTokens,
            int candidateCount,
            double topP,
            int topK,
            String responseMimeType
    ) {}
}
