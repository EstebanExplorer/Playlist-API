package com.esteban.playlistapi.infrastructure.spotify.adapter;

import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.infrastructure.spotify.client.SpotifyClient;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;
import com.esteban.playlistapi.infrastructure.spotify.mapper.SpotifyMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de Infraestructura que implementa el puerto de salida del dominio MusicCatalogPort.
 * Encapsula la comunicación con SpotifyClient y la transformación mediante SpotifyMapper.
 * No expone DTOs de Spotify hacia el exterior de la capa de Infraestructura.
 */
public class SpotifyMusicCatalogAdapter implements MusicCatalogPort {

    private final SpotifyClient spotifyClient;
    private final SpotifyMapper spotifyMapper;

    public SpotifyMusicCatalogAdapter(SpotifyClient spotifyClient, SpotifyMapper spotifyMapper) {
        this.spotifyClient = Objects.requireNonNull(spotifyClient, "SpotifyClient no puede ser nulo.");
        this.spotifyMapper = Objects.requireNonNull(spotifyMapper, "SpotifyMapper no puede ser nulo.");
    }

    @Override
    public Optional<Song> getSongBySpotifyId(String spotifyId) {
        if (spotifyId == null || spotifyId.isBlank()) {
            return Optional.empty();
        }

        Optional<SpotifyTrackItem> trackOpt = spotifyClient.fetchTrackById(spotifyId);
        return trackOpt.map(spotifyMapper::toDomain);
    }

    @Override
    public List<Song> searchSongs(String query) {
        return searchSongs(query, 20);
    }

    @Override
    public List<Song> searchSongs(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<SpotifyTrackItem> trackItems = spotifyClient.searchTracks(query, limit);
        return trackItems.stream()
                .map(spotifyMapper::toDomain)
                .filter(Objects::nonNull)
                .toList();
    }
}
