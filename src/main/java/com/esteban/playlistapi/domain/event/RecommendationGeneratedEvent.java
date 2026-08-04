package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class RecommendationGeneratedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID playlistId;
    private final int count;

    public RecommendationGeneratedEvent(UUID playlistId, int count) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.playlistId = playlistId;
        this.count = count;
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

    public int getCount() {
        return count;
    }
}
