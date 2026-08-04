package com.esteban.playlistapi.infrastructure.spotify.config;

import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.infrastructure.spotify.adapter.SpotifyMusicCatalogAdapter;
import com.esteban.playlistapi.infrastructure.spotify.client.SpotifyClient;
import com.esteban.playlistapi.infrastructure.spotify.mapper.SpotifyMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración de infraestructura para la integración con Spotify.
 * Habilita SpotifyProperties y registra los Beans requeridos para RestClient,
 * SpotifyClient, SpotifyMapper y SpotifyMusicCatalogAdapter.
 */
@Configuration
@EnableConfigurationProperties(SpotifyProperties.class)
public class SpotifyConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public SpotifyMapper spotifyMapper() {
        return new SpotifyMapper();
    }

    @Bean
    public SpotifyClient spotifyClient(RestClient.Builder restClientBuilder, SpotifyProperties spotifyProperties) {
        return new SpotifyClient(restClientBuilder, spotifyProperties);
    }

    @Bean
    public MusicCatalogPort musicCatalogPort(SpotifyClient spotifyClient, SpotifyMapper spotifyMapper) {
        return new SpotifyMusicCatalogAdapter(spotifyClient, spotifyMapper);
    }
}
