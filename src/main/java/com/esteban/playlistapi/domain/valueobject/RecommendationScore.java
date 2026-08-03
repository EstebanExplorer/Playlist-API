package com.esteban.playlistapi.domain.valueobject;

import java.util.Objects;

/**
 * Objeto de Valor que representa la relevancia (score entre 0.0 y 1.0) de una recomendación de IA.
 */
public final class RecommendationScore {

    private final double value;

    public RecommendationScore(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("El score de recomendación debe estar entre 0.0 y 1.0.");
        }
        this.value = value;
    }

    public static RecommendationScore of(double value) {
        return new RecommendationScore(value);
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationScore that = (RecommendationScore) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
