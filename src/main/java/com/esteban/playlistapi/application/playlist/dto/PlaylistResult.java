package com.esteban.playlistapi.application.playlist.dto;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de salida estándar (Result) para representar una Playlist en la capa Application.
 * Reutilizable en operaciones de lectura y escritura (Crear, Consultar por ID, Listar, Actualizar).
 * Record Java 21 inmutable.
 */
public record PlaylistResult(
        UUID id,
        String name,
        UUID userId,
        int songCount,
        List<SongItemResult> songs,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PlaylistResult fromDomain(Playlist playlist) {
        Objects.requireNonNull(playlist, "playlist no puede ser nula");
        List<SongItemResult> songResults = playlist.getSongs() != null ?
                playlist.getSongs().stream()
                        .map(SongItemResult::fromDomain)
                        .toList() : List.of();

        return new PlaylistResult(
                playlist.getId(),
                playlist.getName().getValue(),
                playlist.getUserId(),
                playlist.getSongs().size(),
                songResults,
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }

    /**
     * Record DTO interno que representa un elemento de canción dentro de PlaylistResult.
     */
    public record SongItemResult(
            UUID songId,
            String spotifyId,
            String title,
            String artist,
            String album,
            int durationSeconds
    ) {
        public static SongItemResult fromDomain(Song song) {
            Objects.requireNonNull(song, "song no puede ser nula");
            return new SongItemResult(
                    song.getId(),
                    song.getSpotifyId(),
                    song.getTitle().getValue(),
                    song.getArtist().getValue(),
                    song.getAlbum().getValue(),
                    song.getDuration().getSeconds()
            );
        }
    }
}

