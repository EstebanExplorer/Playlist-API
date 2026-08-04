package com.esteban.playlistapi.domain.valueobject;

import com.esteban.playlistapi.domain.exception.InvalidSongDataException;

import java.util.Objects;

/**
 * Objeto de Valor que representa el título de una canción.
 */
public final class SongTitle {

    private final String value;

    public SongTitle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidSongDataException("El título de la canción no puede estar vacío.");
        }
        this.value = value.trim();
    }

    public static SongTitle of(String value) {
        return new SongTitle(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongTitle songTitle = (SongTitle) o;
        return Objects.equals(value, songTitle.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
