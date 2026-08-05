package com.esteban.playlistapi.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO de respuesta de la API de Google Gemini (generateContent endpoint).
 * Mapea la estructura JSON completa de respuesta incluyendo candidates y usageMetadata.
 *
 * <p>Los campos desconocidos son ignorados explícitamente para garantizar
 * compatibilidad hacia adelante con futuras versiones de la API.
 *
 * <p>Estructura JSON de respuesta de Gemini:
 * <pre>
 * {
 *   "candidates": [{
 *     "content": { "parts": [{"text": "..."}], "role": "model" },
 *     "finishReason": "STOP",
 *     "index": 0
 *   }],
 *   "usageMetadata": {
 *     "promptTokenCount": 120,
 *     "candidatesTokenCount": 85,
 *     "totalTokenCount": 205
 *   }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(
        List<GeminiCandidate> candidates,
        UsageMetadata usageMetadata
) {

    /**
     * Extrae el texto de la primera parte del primer candidato de forma defensiva.
     *
     * @return el texto extraído, o cadena vacía si la respuesta está incompleta o es nula
     */
    public String extractFirstCandidateText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        GeminiCandidate candidate = candidates.get(0);
        if (candidate == null || candidate.content() == null) {
            return "";
        }
        List<GeminiPart> parts = candidate.content().parts();
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        String text = parts.get(0).text();
        return (text != null) ? text : "";
    }

    /**
     * @return true si la respuesta contiene al menos un candidato con texto
     */
    public boolean hasValidCandidate() {
        return !extractFirstCandidateText().isBlank();
    }

    /** Candidato de respuesta generado por el modelo. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiCandidate(GeminiContent content, String finishReason) {}

    /** Contenido del candidato con el texto generado. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiContent(List<GeminiPart> parts, String role) {}

    /** Fragmento de texto dentro del contenido generado. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiPart(String text) {}

    /**
     * Metadatos de uso de tokens retornados por Gemini.
     * Usados para logging de consumo de tokens (diagnóstico, no exposición al cliente).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsageMetadata(
            int promptTokenCount,
            int candidatesTokenCount,
            int totalTokenCount
    ) {
        /** @return representación legible para logging */
        public String toLogString() {
            return String.format("prompt=%d, candidates=%d, total=%d",
                    promptTokenCount, candidatesTokenCount, totalTokenCount);
        }
    }
}
