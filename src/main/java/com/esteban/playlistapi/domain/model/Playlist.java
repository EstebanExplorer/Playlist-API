package com.esteban.playlistapi.domain.model;

import com.esteban.playlistapi.domain.event.DomainEvent;
import com.esteban.playlistapi.domain.event.PlaylistCreatedEvent;
import com.esteban.playlistapi.domain.event.PlaylistUpdatedEvent;
import com.esteban.playlistapi.domain.event.SongAddedEvent;
import com.esteban.playlistapi.domain.event.SongRemovedEvent;
import com.esteban.playlistapi.domain.exception.DuplicateSongException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.valueobject.PlaylistName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del Dominio que representa una Playlist musical.
 * Controla toda la consistencia e invariantes sobre la colección de canciones.
 */
public class Playlist {

    private final UUID id;
    private PlaylistName name;
    private final UUID userId;
    private final List<Song> songs;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Playlist(UUID id, PlaylistName name, UUID userId, List<Song> songs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la playlist no puede ser nulo.");
        }
        if (name == null) {
            throw new IllegalArgumentException("El nombre de la playlist no puede ser nulo.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario propietario no puede ser nulo.");
        }
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.songs = (songs != null) ? new ArrayList<>(songs) : new ArrayList<>();
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
    }

    public static Playlist create(String name, UUID userId) {
        PlaylistName playlistName = PlaylistName.of(name);
        UUID playlistId = UUID.randomUUID();
        Playlist playlist = new Playlist(playlistId, playlistName, userId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now());
        playlist.registerEvent(new PlaylistCreatedEvent(playlistId, playlistName.getValue(), userId));
        return playlist;
    }

    public void updateName(String newName) {
        PlaylistName updatedName = PlaylistName.of(newName);
        if (!this.name.equals(updatedName)) {
            this.name = updatedName;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new PlaylistUpdatedEvent(this.id, updatedName.getValue()));
        }
    }

    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("La canción a agregar no puede ser nula.");
        }
        boolean exists = songs.stream().anyMatch(s ->
                s.getId().equals(song.getId()) ||
                (!s.getSpotifyId().isEmpty() && s.getSpotifyId().equalsIgnoreCase(song.getSpotifyId()))
        );
        if (exists) {
            throw new DuplicateSongException(song.getTitle().getValue());
        }
        this.songs.add(song);
        this.updatedAt = LocalDateTime.now();
        registerEvent(new SongAddedEvent(this.id, song.getId()));
    }

    public void removeSong(UUID songId) {
        if (songId == null) {
            throw new IllegalArgumentException("El ID de la canción no puede ser nulo.");
        }
        boolean removed = this.songs.removeIf(s -> s.getId().equals(songId));
        if (!removed) {
            throw new SongNotFoundException(songId);
        }
        this.updatedAt = LocalDateTime.now();
        registerEvent(new SongRemovedEvent(this.id, songId));
    }

    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    public boolean hasSong(UUID songId) {
        return songs.stream().anyMatch(s -> s.getId().equals(songId));
    }

    public UUID getId() {
        return id;
    }

    public PlaylistName getName() {
        return name;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
