package com.esteban.playlistapi.domain.valueobject;

import com.esteban.playlistapi.domain.exception.InvalidSongDataException;

import java.util.Objects;

/**
 * Objeto de Valor que representa la duración en segundos de una canción.
 */
public final class Duration {

    private final int seconds;

    public Duration(int seconds) {
        if (seconds < 0) {
            throw new InvalidSongDataException("La duración de la canción no puede ser negativa.");
        }
        this.seconds = seconds;
    }

    public static Duration ofSeconds(int seconds) {
        return new Duration(seconds);
    }

    public int getSeconds() {
        return seconds;
    }

    public String getFormatted() {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Duration duration = (Duration) o;
        return seconds == duration.seconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seconds);
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}
