package com.esteban.playlistapi.domain.service;

import com.esteban.playlistapi.domain.exception.UnauthorizedPlaylistAccessException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;

import java.util.UUID;

/**
 * Servicio del Dominio encargado de coordinar reglas de negocio puras
 * que involucran múltiples entidades o validaciones cruzadas de accesos.
 */
public class PlaylistDomainService {

    public void validateOwnership(Playlist playlist, UUID userId) {
        if (!playlist.isOwnedBy(userId)) {
            throw new UnauthorizedPlaylistAccessException(userId, playlist.getId());
        }
    }

    public void addSongToPlaylist(Playlist playlist, Song song, UUID requestingUserId) {
        validateOwnership(playlist, requestingUserId);
        playlist.addSong(song);
    }

    public void removeSongFromPlaylist(Playlist playlist, UUID songId, UUID requestingUserId) {
        validateOwnership(playlist, requestingUserId);
        playlist.removeSong(songId);
    }

    public void updatePlaylistName(Playlist playlist, String newName, UUID requestingUserId) {
        validateOwnership(playlist, requestingUserId);
        playlist.updateName(newName);
    }
}
