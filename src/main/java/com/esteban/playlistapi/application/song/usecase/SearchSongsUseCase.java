package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.song.dto.SearchSongsQuery;
import com.esteban.playlistapi.application.song.dto.SongSearchResult;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import java.util.List;
import java.util.Objects;

/**
 * Caso de Uso UC-009: Consultar Información Musical (Search Songs).
 * Consulta canciones en el catálogo musical externo a través del puerto MusicCatalogPort.
 * Operación CQRS de lectura pura (Query). Pertenece a la capa Application.
 */
public class SearchSongsUseCase {

    private final MusicCatalogPort musicCatalogPort;

    public SearchSongsUseCase(MusicCatalogPort musicCatalogPort) {
        this.musicCatalogPort = Objects.requireNonNull(musicCatalogPort, "musicCatalogPort no puede ser nulo");
    }

    public List<SongSearchResult> execute(SearchSongsQuery query) {
        if (query == null) {
            throw new InvalidQueryException("La consulta de búsqueda no puede ser nula.");
        }

        List<Song> songs = Objects.requireNonNullElse(
                musicCatalogPort.searchSongs(query.query(), query.limit()),
                List.of()
        );

        return songs.stream()
                .map(SongSearchResult::fromDomain)
                .toList();
    }
}
