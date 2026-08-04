package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.AddSongCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.service.PlaylistDomainService;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import java.util.Objects;

/**
 * Caso de Uso UC-007: Agregar Canción a Playlist.
 * Orquesta la adición de una canción existente del catálogo musical a una playlist del dominio.
 * Pertenece a la capa Application. Pura implementación Java sin dependencias de frameworks.
 */
public class AddSongToPlaylistUseCase {

    private final PlaylistRepository playlistRepository;
    private final MusicCatalogPort musicCatalogPort;
    private final PlaylistDomainService playlistDomainService;

    public AddSongToPlaylistUseCase(PlaylistRepository playlistRepository,
                                   MusicCatalogPort musicCatalogPort,
                                   PlaylistDomainService playlistDomainService) {
        this.playlistRepository = Objects.requireNonNull(playlistRepository, "playlistRepository no puede ser nulo");
        this.musicCatalogPort = Objects.requireNonNull(musicCatalogPort, "musicCatalogPort no puede ser nulo");
        this.playlistDomainService = Objects.requireNonNull(playlistDomainService, "playlistDomainService no puede ser nulo");
    }

    public PlaylistResult execute(AddSongCommand command) {
        if (command == null) {
            throw new InvalidCommandException("El comando para agregar la canción a la playlist no puede ser nulo.");
        }

        Playlist playlist = playlistRepository.findById(command.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException(command.playlistId()));

        Song song = musicCatalogPort.getSongBySpotifyId(command.songId())
                .orElseThrow(() -> new SongNotFoundException(command.songId()));

        playlist.addSong(song);

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return PlaylistResult.fromDomain(savedPlaylist);
    }
}
