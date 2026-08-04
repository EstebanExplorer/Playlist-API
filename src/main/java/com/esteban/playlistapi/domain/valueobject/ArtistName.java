package com.esteban.playlistapi.domain.valueobject;

import com.esteban.playlistapi.domain.exception.InvalidSongDataException;

import java.util.Objects;

/**
 * Objeto de Valor que representa el nombre del artista de una canción.
 */
public final class ArtistName {

    private final String value;

    public ArtistName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidSongDataException("El nombre del artista no puede estar vacío.");
        }
        this.value = value.trim();
    }

    public static ArtistName of(String value) {
        return new ArtistName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistName that = (ArtistName) o;
        return Objects.equals(value, that.value);
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
