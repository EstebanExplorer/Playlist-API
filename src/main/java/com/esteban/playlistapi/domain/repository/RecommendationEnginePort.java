package com.esteban.playlistapi.domain.repository;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;

import java.util.List;

/**
 * Puerto de Salida (Output Port) del Dominio para la generación de recomendaciones musicales.
 * Define la abstracción necesaria para calcular recomendaciones para una playlist determinada.
 */
public interface RecommendationEnginePort {

    List<Recommendation> generateRecommendations(Playlist playlist, int limit);
}
