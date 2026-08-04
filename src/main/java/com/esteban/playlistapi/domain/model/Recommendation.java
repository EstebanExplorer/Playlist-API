package com.esteban.playlistapi.domain.model;

import com.esteban.playlistapi.domain.valueobject.ArtistName;
import com.esteban.playlistapi.domain.valueobject.RecommendationScore;
import com.esteban.playlistapi.domain.valueobject.SongTitle;

import java.util.Objects;

/**
 * Objeto de Dominio que representa una recomendación generada por IA.
 */
public class Recommendation {

    private final SongTitle songTitle;
    private final ArtistName artistName;
    private final String reasoning;
    private final RecommendationScore score;

    public Recommendation(SongTitle songTitle, ArtistName artistName, String reasoning, RecommendationScore score) {
        if (songTitle == null) {
            throw new IllegalArgumentException("El título de la canción recomendada no puede ser nulo.");
        }
        if (artistName == null) {
            throw new IllegalArgumentException("El artista de la canción recomendada no puede ser nulo.");
        }
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.reasoning = (reasoning != null) ? reasoning.trim() : "";
        this.score = (score != null) ? score : RecommendationScore.of(1.0);
    }

    public static Recommendation create(String songTitle, String artistName, String reasoning, double score) {
        return new Recommendation(
                SongTitle.of(songTitle),
                ArtistName.of(artistName),
                reasoning,
                RecommendationScore.of(score)
        );
    }

    public SongTitle getSongTitle() {
        return songTitle;
    }

    public ArtistName getArtistName() {
        return artistName;
    }

    public String getReasoning() {
        return reasoning;
    }

    public RecommendationScore getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return Objects.equals(songTitle, that.songTitle) &&
               Objects.equals(artistName, that.artistName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songTitle, artistName);
    }
}
