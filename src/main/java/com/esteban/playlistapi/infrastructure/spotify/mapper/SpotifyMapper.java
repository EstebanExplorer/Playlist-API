package com.esteban.playlistapi.infrastructure.spotify.mapper;

import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyArtistItem;
import com.esteban.playlistapi.infrastructure.spotify.dto.SpotifyTrackItem;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper manual de Infraestructura.
 * Convierte DTOs de Spotify (SpotifyTrackItem) a la entidad del Dominio (Song).
 * No utiliza MapStruct, ModelMapper ni reflexión.
 */
public class SpotifyMapper {

    public Song toDomain(SpotifyTrackItem item) {
        if (item == null) {
            return null;
        }

        String spotifyId = item.id() != null ? item.id().trim() : "";
        String title = (item.name() != null && !item.name().isBlank()) ? item.name().trim() : "Sin Título";

        String artistName = "Artista Desconocido";
        if (item.artists() != null && !item.artists().isEmpty()) {
            artistName = item.artists().stream()
                    .map(SpotifyArtistItem::name)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(", "));
            if (artistName.isBlank()) {
                artistName = "Artista Desconocido";
            }
        }

        String albumName = "Álbum Desconocido";
        if (item.album() != null && item.album().name() != null && !item.album().name().isBlank()) {
            albumName = item.album().name().trim();
        }

        int durationSeconds = (int) Math.max(1, item.durationMs() / 1000);

        return Song.create(spotifyId, title, artistName, albumName, durationSeconds);
    }
}
