package com.esteban.playlistapi.domain.model;

import com.esteban.playlistapi.domain.valueobject.AlbumName;
import com.esteban.playlistapi.domain.valueobject.ArtistName;
import com.esteban.playlistapi.domain.valueobject.Duration;
import com.esteban.playlistapi.domain.valueobject.SongTitle;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del dominio que representa una Canción musical.
 * Posee identidad propia (UUID) e identificador externo (spotifyId).
 */
public class Song {

    private final UUID id;
    private final String spotifyId;
    private final SongTitle title;
    private final ArtistName artist;
    private final AlbumName album;
    private final Duration duration;

    public Song(UUID id, String spotifyId, SongTitle title, ArtistName artist, AlbumName album, Duration duration) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la canción no puede ser nulo.");
        }
        if (title == null) {
            throw new IllegalArgumentException("El título de la canción no puede ser nulo.");
        }
        if (artist == null) {
            throw new IllegalArgumentException("El artista de la canción no puede ser nulo.");
        }
        this.id = id;
        this.spotifyId = (spotifyId != null) ? spotifyId.trim() : "";
        this.title = title;
        this.artist = artist;
        this.album = (album != null) ? album : AlbumName.of("");
        this.duration = (duration != null) ? duration : Duration.ofSeconds(0);
    }

    public static Song create(String spotifyId, String title, String artist, String album, int durationSeconds) {
        return new Song(
                UUID.randomUUID(),
                spotifyId,
                SongTitle.of(title),
                ArtistName.of(artist),
                AlbumName.of(album),
                Duration.ofSeconds(durationSeconds)
        );
    }

    public UUID getId() {
        return id;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public SongTitle getTitle() {
        return title;
    }

    public ArtistName getArtist() {
        return artist;
    }

    public AlbumName getAlbum() {
        return album;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
