package com.esteban.playlistapi.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA relacional que mapea la entidad Song del dominio a la tabla relacional 'playlist_songs'.
 */
@Entity
@Table(name = "playlist_songs", indexes = {
        @Index(name = "idx_playlist_songs_playlist_id", columnList = "playlist_id"),
        @Index(name = "idx_playlist_songs_spotify_id", columnList = "spotify_id")
})
public class SongJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "spotify_id", nullable = false, length = 100)
    private String spotifyId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "artist", nullable = false, length = 150)
    private String artist;

    @Column(name = "album", nullable = false, length = 150)
    private String album;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private PlaylistJpaEntity playlist;

    public SongJpaEntity() {
    }

    public SongJpaEntity(UUID id, String spotifyId, String title, String artist, String album, int durationSeconds, PlaylistJpaEntity playlist) {
        this.id = id;
        this.spotifyId = spotifyId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationSeconds = durationSeconds;
        this.playlist = playlist;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public PlaylistJpaEntity getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlaylistJpaEntity playlist) {
        this.playlist = playlist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongJpaEntity that = (SongJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
