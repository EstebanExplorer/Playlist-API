package com.esteban.playlistapi.infrastructure.spotify.adapter;

import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de prueba/fallback para el puerto MusicCatalogPort.
 * Pertenece a la capa Infrastructure.
 */
@Component
public class DummyMusicCatalogAdapter implements MusicCatalogPort {

    @Override
    public Optional<Song> getSongBySpotifyId(String spotifyId) {
        if (spotifyId == null || spotifyId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Song.create(spotifyId, "Cancion de Prueba", "Artista de Prueba", "Album de Prueba", 180));
    }

    @Override
    public List<Song> searchSongs(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return List.of(
                Song.create("spotify-001", "Cancion " + query, "Artista de Prueba", "Album de Prueba", 200)
        );
    }
}
