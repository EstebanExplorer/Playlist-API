package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.RemoveSongCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.Objects;

/**
 * Caso de Uso UC-008: Remover Canción de Playlist.
 * Orquesta la eliminación de una canción existente de una playlist del dominio.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class RemoveSongFromPlaylistUseCase {

    private final PlaylistRepository playlistRepository;

    public RemoveSongFromPlaylistUseCase(PlaylistRepository playlistRepository) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
    }

    public PlaylistResult execute(RemoveSongCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para remover la canción de la playlist no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        playlist.removeSong(command.songId());

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return PlaylistResult.fromDomain(savedPlaylist);
    }
}
