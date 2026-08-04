package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.GetPlaylistByIdQuery;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import java.util.Objects;

/**
 * Caso de Uso UC-003: Consultar Playlist por ID.
 * Obtiene la información detallada de una playlist existente a través de su puerto de persistencia.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class GetPlaylistByIdUseCase {

    private final PlaylistRepository playlistRepository;

    public GetPlaylistByIdUseCase(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
    }

    public PlaylistResult execute(GetPlaylistByIdQuery query) {
        if (query == null) {
            throw new InvalidQueryException("La consulta para obtener la playlist no puede ser nula.");
        }

        Playlist playlist = playlistRepository.findById(query.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(query.playlistId()));

        return PlaylistResult.fromDomain(playlist);
    }
}
