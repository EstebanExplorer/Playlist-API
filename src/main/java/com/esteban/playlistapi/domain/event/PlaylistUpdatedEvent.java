package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PlaylistUpdatedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID playlistId;
    private final String newName;

    public PlaylistUpdatedEvent(UUID playlistId, String newName) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.playlistId = playlistId;
        this.newName = newName;
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

    public String getNewName() {
        return newName;
    }
}
