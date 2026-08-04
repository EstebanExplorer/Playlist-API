package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.CreatePlaylistCommand;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.domain.exception.UserNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.Objects;

/**
 * Caso de Uso UC-002: Crear Playlist.
 * Orquesta la creación de una nueva playlist para un usuario existente.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class CreatePlaylistUseCase {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    public CreatePlaylistUseCase(PlaylistRepository playlistRepository, UserRepository userRepository) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository no puede ser nulo");
    }

    public PlaylistResult execute(CreatePlaylistCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para crear la playlist no puede ser nulo.");
        }

        userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Playlist playlist = Playlist.create(command.name(), command.userId());

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return PlaylistResult.fromDomain(savedPlaylist);
    }
}
