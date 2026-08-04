package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.playlist.dto.UpdatePlaylistNameCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.service.PlaylistDomainService;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.Objects;

/**
 * Caso de Uso UC-005: Actualizar Nombre de Playlist.
 * Orquesta la actualización del nombre de una playlist existente delegando la validación e invariantes al Dominio.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class UpdatePlaylistNameUseCase {

    private final PlaylistRepository playlistRepository;
    private final PlaylistDomainService playlistDomainService;

    public UpdatePlaylistNameUseCase(PlaylistRepository playlistRepository, PlaylistDomainService playlistDomainService) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
        this.playlistDomainService = Objects.requireNonNull(playlistDomainService, "playlistDomainService no puede ser nulo");
    }

    public PlaylistResult execute(UpdatePlaylistNameCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para actualizar el nombre de la playlist no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        playlist.updateName(command.newName());

        Playlist updatedPlaylist = playlistRepository.save(playlist);

        return PlaylistResult.fromDomain(updatedPlaylist);
    }
}
