package com.esteban.playlistapi.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID userId) {
        super("No se encontró el usuario con ID: " + userId);
    }

    public UserNotFoundException(String username) {
        super("No se encontró el usuario: " + username);
    }
}
