package com.esteban.playlistapi.application.song.dto;

import com.esteban.playlistapi.domain.model.Song;

import java.util.Objects;
import java.util.UUID;

/**
 * DTO de salida (Result) para representar los resultados del catálogo de canciones en UC-009.
 * Implementado como un Record Java 21 inmutable.
 */
public record SongSearchResult(
        UUID songId,
        String spotifyId,
        String title,
        String artist,
        String album,
        int durationSeconds
) {
    public static SongSearchResult fromDomain(Song song) {
        Objects.requireNonNull(song, "song no puede ser nulo");
        return new SongSearchResult(
                song.getId(),
                song.getSpotifyId(),
                song.getTitle().getValue(),
                song.getArtist().getValue(),
                song.getAlbum().getValue(),
                song.getDuration().getSeconds()
        );
    }
}
