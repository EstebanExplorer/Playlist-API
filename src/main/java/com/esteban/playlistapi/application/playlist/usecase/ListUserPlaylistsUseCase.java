package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.ListUserPlaylistsQuery;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import java.util.List;
import java.util.Objects;

/**
 * Caso de Uso UC-004: Listar Playlists por Usuario.
 * Consulta y retorna la colección de playlists pertenecientes a un usuario autenticado.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class ListUserPlaylistsUseCase {

    private final PlaylistRepository playlistRepository;

    public ListUserPlaylistsUseCase(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
    }

    public List<PlaylistResult> execute(ListUserPlaylistsQuery query) {
        if (query == null) {
            throw new InvalidQueryException("La consulta para listar las playlists no puede ser nula.");
        }

        List<Playlist> playlists = Objects.requireNonNullElse(
                playlistRepository.findByUserId(query.userId()),
                List.of()
        );

        return playlists.stream()
                .map(PlaylistResult::fromDomain)
                .toList();
    }
}
