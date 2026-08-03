package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class SongAddedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID playlistId;
    private final UUID songId;

    public SongAddedEvent(UUID playlistId, UUID songId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.playlistId = playlistId;
        this.songId = songId;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public UUID getPlaylistId() {
        return playlistId;
    }

    public UUID getSongId() {
        return songId;
    }
}
