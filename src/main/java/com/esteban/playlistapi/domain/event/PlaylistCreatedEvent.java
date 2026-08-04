package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PlaylistCreatedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID playlistId;
    private final String name;
    private final UUID userId;

    public PlaylistCreatedEvent(UUID playlistId, String name, UUID userId) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.playlistId = playlistId;
        this.name = name;
        this.userId = userId;
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

    public String getName() {
        return name;
    }

    public UUID getUserId() {
        return userId;
    }
}
