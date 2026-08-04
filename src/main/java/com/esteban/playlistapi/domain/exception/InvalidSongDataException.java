package com.esteban.playlistapi.domain.exception;

public class InvalidSongDataException extends DomainException {
    public InvalidSongDataException(String message) {
        super(message);
    }
}
