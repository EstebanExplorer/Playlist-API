package com.esteban.playlistapi.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA relacional que mapea el Agregado Playlist a la tabla de base de datos 'playlists'.
 */
@Entity
@Table(name = "playlists", indexes = {
        @Index(name = "idx_playlists_user_id", columnList = "user_id")
})
public class PlaylistJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SongJpaEntity> songs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlaylistJpaEntity() {
    }

    public PlaylistJpaEntity(UUID id, String name, UUID userId, List<SongJpaEntity> songs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        if (songs != null) {
            setSongs(songs);
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addSong(SongJpaEntity song) {
        songs.add(song);
        song.setPlaylist(this);
    }

    public void removeSong(SongJpaEntity song) {
        songs.remove(song);
        song.setPlaylist(null);
    }

    public void setSongs(List<SongJpaEntity> songs) {
        this.songs.clear();
        if (songs != null) {
            songs.forEach(this::addSong);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<SongJpaEntity> getSongs() {
        return songs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistJpaEntity that = (PlaylistJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
