package com.esteban.playlistapi.infrastructure.recommendation.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptador de Infraestructura que implementa el puerto RecommendationEnginePort.
 * Calcula recomendaciones de canciones basadas en reglas de negocio utilizando el catálogo externo de Spotify (MusicCatalogPort).
 * Anotado como @Primary para ser la implementación por defecto en el contenedor IoC.
 */
@Component
@Primary
public class RuleBasedRecommendationAdapter implements RecommendationEnginePort, AiRecommendationPort {

    private final MusicCatalogPort musicCatalogPort;

    public RuleBasedRecommendationAdapter(MusicCatalogPort musicCatalogPort) {
        this.musicCatalogPort = Objects.requireNonNull(musicCatalogPort, "musicCatalogPort no puede ser nulo");
    }

    @Override
    public List<Recommendation> generateRecommendations(Playlist playlist, int limit) {
        if (playlist == null) {
            return List.of();
        }

        int validLimit = (limit <= 0) ? 10 : Math.min(limit, 20);

        List<Song> existingSongs = playlist.getSongs() != null ? playlist.getSongs() : List.of();
        Set<String> existingSpotifyIds = existingSongs.stream()
                .map(Song::getSpotifyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingTitles = existingSongs.stream()
                .map(s -> s.getTitle().getValue().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<String> topArtists = findTopArtists(existingSongs);

        List<Recommendation> recommendations = new ArrayList<>();
        Set<String> addedRecommendationKeys = new HashSet<>();

        if (!topArtists.isEmpty()) {
            for (String artist : topArtists) {
                if (recommendations.size() >= validLimit) {
                    break;
                }
                List<Song> candidateSongs = musicCatalogPort.searchSongs(artist, 10);
                for (Song candidate : candidateSongs) {
                    if (recommendations.size() >= validLimit) {
                        break;
                    }
                    if (isAlreadyInPlaylist(candidate, existingSpotifyIds, existingTitles)) {
                        continue;
                    }
                    String key = candidate.getArtist().getValue().toLowerCase(Locale.ROOT) + ":"
                            + candidate.getTitle().getValue().toLowerCase(Locale.ROOT);
                    if (addedRecommendationKeys.contains(key)) {
                        continue;
                    }

                    addedRecommendationKeys.add(key);
                    double score = Math.max(0.70, 0.95 - (recommendations.size() * 0.03));
                    recommendations.add(Recommendation.create(
                            candidate.getTitle().getValue(),
                            candidate.getArtist().getValue(),
                            "Basado en tu preferencia por el artista " + artist,
                            score
                    ));
                }
            }
        }

        // Si la playlist está vacía o no se alcanzaron suficientes recomendaciones por artista
        if (recommendations.size() < validLimit) {
            List<String> fallbackQueries = List.of("Top Hits", "Rock", "Pop", "Indie");
            for (String query : fallbackQueries) {
                if (recommendations.size() >= validLimit) {
                    break;
                }
                List<Song> candidateSongs = musicCatalogPort.searchSongs(query, 10);
                for (Song candidate : candidateSongs) {
                    if (recommendations.size() >= validLimit) {
                        break;
                    }
                    if (isAlreadyInPlaylist(candidate, existingSpotifyIds, existingTitles)) {
                        continue;
                    }
                    String key = candidate.getArtist().getValue().toLowerCase(Locale.ROOT) + ":"
                            + candidate.getTitle().getValue().toLowerCase(Locale.ROOT);
                    if (addedRecommendationKeys.contains(key)) {
                        continue;
                    }

                    addedRecommendationKeys.add(key);
                    double score = Math.max(0.60, 0.85 - (recommendations.size() * 0.03));
                    recommendations.add(Recommendation.create(
                            candidate.getTitle().getValue(),
                            candidate.getArtist().getValue(),
                            "Recomendación popular del catálogo musical (" + query + ")",
                            score
                    ));
                }
            }
        }

        return recommendations.stream()
                .limit(validLimit)
                .toList();
    }

    private List<String> findTopArtists(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return List.of();
        }
        Map<String, Long> artistCounts = songs.stream()
                .filter(s -> s.getArtist() != null && s.getArtist().getValue() != null)
                .collect(Collectors.groupingBy(s -> s.getArtist().getValue(), Collectors.counting()));

        return artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .toList();
    }

    private boolean isAlreadyInPlaylist(Song candidate, Set<String> existingSpotifyIds, Set<String> existingTitles) {
        if (candidate == null) {
            return true;
        }
        if (candidate.getSpotifyId() != null && existingSpotifyIds.contains(candidate.getSpotifyId())) {
            return true;
        }
        if (candidate.getTitle() != null && existingTitles.contains(candidate.getTitle().getValue().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
    }
}
