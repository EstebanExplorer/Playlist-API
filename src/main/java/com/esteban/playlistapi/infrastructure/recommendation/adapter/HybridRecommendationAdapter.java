package com.esteban.playlistapi.infrastructure.recommendation.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;
import com.esteban.playlistapi.infrastructure.ai.adapter.GeminiRecommendationAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador Híbrido de Recomendaciones: implementación primaria del sistema
 * de recomendaciones musicales que combina Google Gemini con un fallback automático
 * basado en reglas (RuleBasedRecommendationAdapter).
 *
 * <p>Estrategia de resiliencia:
 * <ol>
 *   <li>Intenta generar recomendaciones con {@link GeminiRecommendationAdapter}</li>
 *   <li>Si Gemini retorna lista no vacía → resultado devuelto al usuario</li>
 *   <li>Si Gemini retorna lista vacía → activa fallback a {@link RuleBasedRecommendationAdapter}</li>
 *   <li>Si Gemini lanza cualquier excepción (timeout, HTTP 429, 500, JSON inválido) →
 *       log WARN + fallback automático sin propagar error al usuario</li>
 * </ol>
 *
 * <p>El usuario NUNCA recibe un error causado por la indisponibilidad de Gemini.
 * El fallback garantiza disponibilidad continua del endpoint.
 *
 * <p>Arquitectura Hexagonal: Es el único bean {@code @Primary} para
 * {@link AiRecommendationPort} y {@link RecommendationEnginePort}. Todos los Use Cases
 * y Domain Services que dependen de estas interfaces reciben esta implementación.
 *
 * <p>Este bean es registrado como {@code @Primary} en {@code GeminiConfiguration}.
 */
public class HybridRecommendationAdapter implements RecommendationEnginePort, AiRecommendationPort {

    private static final Logger log = LoggerFactory.getLogger(HybridRecommendationAdapter.class);

    private final GeminiRecommendationAdapter geminiAdapter;
    private final RuleBasedRecommendationAdapter ruleBasedAdapter;

    public HybridRecommendationAdapter(GeminiRecommendationAdapter geminiAdapter,
                                       RuleBasedRecommendationAdapter ruleBasedAdapter) {
        this.geminiAdapter = Objects.requireNonNull(geminiAdapter,
                "GeminiRecommendationAdapter no puede ser nulo.");
        this.ruleBasedAdapter = Objects.requireNonNull(ruleBasedAdapter,
                "RuleBasedRecommendationAdapter no puede ser nulo.");
    }

    /**
     * Genera recomendaciones musicales intentando Gemini primero y aplicando
     * fallback automático a RuleBased si Gemini no está disponible o falla.
     *
     * @param playlist la playlist del usuario para la cual generar recomendaciones
     * @param limit    número máximo de recomendaciones solicitadas
     * @return lista de recomendaciones, nunca null, puede ser vacía solo si ambos fallan
     */
    @Override
    public List<Recommendation> generateRecommendations(Playlist playlist, int limit) {
        try {
            List<Recommendation> geminiResult = geminiAdapter.generateRecommendations(playlist, limit);

            if (geminiResult != null && !geminiResult.isEmpty()) {
                log.info("[HybridAdapter] Gemini generó {} recomendaciones exitosamente para la playlist '{}'.",
                        geminiResult.size(),
                        playlist != null ? playlist.getName().getValue() : "null");
                return geminiResult;
            }

            log.warn("[HybridAdapter] Gemini retornó lista vacía. Activando fallback a RuleBasedRecommendationAdapter.");

        } catch (Exception e) {
            log.warn("[HybridAdapter] Gemini no disponible o falló [{}]. " +
                            "Activando fallback automático a RuleBasedRecommendationAdapter.",
                    e.getMessage());
        }

        return ruleBasedAdapter.generateRecommendations(playlist, limit);
    }
}
