package com.esteban.playlistapi.domain.exception;

public class InvalidPlaylistNameException extends DomainException {
    public InvalidPlaylistNameException(String message) {
        super(message);
    }
}
