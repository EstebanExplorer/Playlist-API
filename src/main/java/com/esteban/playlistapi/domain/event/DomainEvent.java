package com.esteban.playlistapi.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interfaz base para todos los Eventos de Dominio.
 */
public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredOn();
}
