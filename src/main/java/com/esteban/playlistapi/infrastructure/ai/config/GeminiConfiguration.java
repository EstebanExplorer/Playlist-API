package com.esteban.playlistapi.infrastructure.ai.config;

import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;
import com.esteban.playlistapi.infrastructure.ai.adapter.GeminiRecommendationAdapter;
import com.esteban.playlistapi.infrastructure.ai.client.GeminiClient;
import com.esteban.playlistapi.infrastructure.recommendation.adapter.HybridRecommendationAdapter;
import com.esteban.playlistapi.infrastructure.recommendation.adapter.RuleBasedRecommendationAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración de Spring para la integración con Google Gemini.
 * <p>
 * Implementa el patrón Registry para evitar inyecciones ambiguas del puerto
 * {@link RecommendationEnginePort}. El sistema crea dos adaptadores:
 * 1. El adaptador basado en reglas (creado en su propia configuración)
 * 2. El adaptador de Gemini (creado aquí)
 * <p>
 * Luego los envuelve en un {@link HybridRecommendationAdapter} que actúa como
 * un Circuit Breaker, siendo este último el bean @Primary inyectado en el caso de uso.
 */
@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfiguration {

    /**
     * Cliente HTTP encapsulado para Gemini.
     * Configura los timeouts (connect y read) basados en las propiedades externalizadas
     * mediante {@link SimpleClientHttpRequestFactory}.
     */
    @Bean
    public GeminiClient geminiClient(RestClient.Builder restClientBuilder,
                                     GeminiProperties properties,
                                     ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // Fijo en 5 segundos (suficiente para handshake)
        requestFactory.setReadTimeout(properties.getTimeout() * 1000); // Timeout en milisegundos

        RestClient.Builder customBuilder = restClientBuilder.clone()
                .requestFactory(requestFactory);

        return new GeminiClient(customBuilder, properties, objectMapper);
    }

    /**
     * Adaptador que conecta el puerto del dominio con el cliente Gemini.
     * NO está anotado con @Component en la clase para mantenerlo puro en la capa de infraestructura
     * y poder instanciarlo explícitamente aquí.
     */
    @Bean("geminiRecommendationAdapter")
    public GeminiRecommendationAdapter geminiRecommendationAdapter(GeminiClient geminiClient,
                                                                    MusicCatalogPort musicCatalogPort) {
        return new GeminiRecommendationAdapter(geminiClient, musicCatalogPort);
    }

    /**
     * BEAN PRINCIPAL (@Primary) PARA EL CASO DE USO.
     * Implementa el patrón Circuit Breaker manual.
     * Intenta usar Gemini primero, y si falla con CUALQUIER excepción
     * (ExternalServiceException, HttpClientErrorException, timeout, json malformado),
     * captura el error y delega al adaptador basado en reglas (Fallback).
     */
    @Bean
    @Primary
    public HybridRecommendationAdapter primaryHybridRecommendationAdapter(
            @Qualifier("geminiRecommendationAdapter") GeminiRecommendationAdapter geminiAdapter,
            @Qualifier("ruleBasedRecommendationAdapter") RuleBasedRecommendationAdapter ruleBasedAdapter) {
        return new HybridRecommendationAdapter(geminiAdapter, ruleBasedAdapter);
    }
}
