package com.esteban.playlistapi.domain.valueobject;

import com.esteban.playlistapi.domain.exception.InvalidPlaylistNameException;

import java.util.Objects;

/**
 * Objeto de Valor que representa el nombre válido de una playlist.
 * Garantiza las reglas de negocio sobre la validez del nombre.
 */
public final class PlaylistName {

    private static final int MAX_LENGTH = 100;
    private final String value;

    public PlaylistName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPlaylistNameException("El nombre de la playlist no puede estar vacío.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidPlaylistNameException("El nombre de la playlist no puede exceder " + MAX_LENGTH + " caracteres.");
        }
        this.value = trimmed;
    }

    public static PlaylistName of(String value) {
        return new PlaylistName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistName that = (PlaylistName) o;
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
