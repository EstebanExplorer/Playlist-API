package com.esteban.playlistapi.domain.valueobject;

import java.util.Objects;

/**
 * Objeto de Valor opcional que representa el nombre del álbum de una canción.
 */
public final class AlbumName {

    private final String value;

    public AlbumName(String value) {
        this.value = (value != null) ? value.trim() : "";
    }

    public static AlbumName of(String value) {
        return new AlbumName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumName albumName = (AlbumName) o;
        return Objects.equals(value, albumName.value);
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
