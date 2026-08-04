package com.esteban.playlistapi.domain.exception;

import java.util.UUID;

public class SongNotFoundException extends DomainException {
    public SongNotFoundException(UUID songId) {
        super("No se encontró la canción con ID: " + songId);
    }

    public SongNotFoundException(String message) {
        super(message);
    }
}
