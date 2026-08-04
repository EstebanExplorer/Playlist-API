package com.esteban.playlistapi.domain.exception;

import java.util.UUID;

public class PlaylistNotFoundException extends DomainException {
    public PlaylistNotFoundException(UUID playlistId) {
        super("No se encontró la playlist con ID: " + playlistId);
    }

    public PlaylistNotFoundException(String message) {
        super(message);
    }
}
