package com.esteban.playlistapi.domain.exception;

import java.util.UUID;

public class UnauthorizedPlaylistAccessException extends DomainException {
    public UnauthorizedPlaylistAccessException(UUID userId, UUID playlistId) {
        super("El usuario con ID " + userId + " no tiene permisos para acceder o modificar la playlist " + playlistId);
    }
}
