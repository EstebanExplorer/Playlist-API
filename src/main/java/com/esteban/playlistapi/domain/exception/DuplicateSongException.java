package com.esteban.playlistapi.domain.exception;

public class DuplicateSongException extends DomainException {
    public DuplicateSongException(String songTitle) {
        super("La canción '" + songTitle + "' ya existe en esta playlist.");
    }
}
