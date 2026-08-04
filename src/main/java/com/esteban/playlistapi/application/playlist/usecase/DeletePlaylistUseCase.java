package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.DeletePlaylistCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.Objects;

/**
 * Caso de Uso UC-006: Eliminar Playlist.
 * Orquesta la eliminación de una playlist existente a través del puerto de persistencia del dominio.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class DeletePlaylistUseCase {

    private final PlaylistRepository playlistRepository;

    public DeletePlaylistUseCase(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
    }

    public void execute(DeletePlaylistCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para eliminar la playlist no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        playlistRepository.delete(playlist);
    }
}
