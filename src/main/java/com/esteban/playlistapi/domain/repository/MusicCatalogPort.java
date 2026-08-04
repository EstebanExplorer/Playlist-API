package com.esteban.playlistapi.domain.repository;

import com.esteban.playlistapi.domain.model.Song;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Output Port) para consultas al catálogo de música externo (Spotify).
 */
public interface MusicCatalogPort {

    Optional<Song> getSongBySpotifyId(String spotifyId);

    List<Song> searchSongs(String query);

    default List<Song> searchSongs(String query, int limit) {
        return searchSongs(query);
    }
}
