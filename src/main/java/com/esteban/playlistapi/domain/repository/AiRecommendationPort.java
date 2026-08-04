package com.esteban.playlistapi.domain.repository;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;

import java.util.List;

/**
 * Puerto de Salida (Output Port) para generar recomendaciones musicales mediante un proveedor de Inteligencia Artificial.
 */
public interface AiRecommendationPort {

    List<Recommendation> generateRecommendations(Playlist playlist, int limit);
}
