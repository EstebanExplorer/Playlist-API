package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PlaylistDeletedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID playlistId;

    public PlaylistDeletedEvent(UUID playlistId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.playlistId = playlistId;
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
}
