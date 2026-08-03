package com.esteban.playlistapi.domain.model;

import com.esteban.playlistapi.domain.exception.DuplicateSongException;
import com.esteban.playlistapi.domain.exception.InvalidPlaylistNameException;
import com.esteban.playlistapi.domain.exception.SongNotFoundException;
import com.esteban.playlistapi.domain.valueobject.PlaylistName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    @Test
    @DisplayName("Debe crear una playlist válida y registrar un evento de dominio")
    void createValidPlaylist() {
        UUID userId = UUID.randomUUID();
        Playlist playlist = Playlist.create("My Rock Playlist", userId);

        assertNotNull(playlist.getId());
        assertEquals("My Rock Playlist", playlist.getName().getValue());
        assertEquals(userId, playlist.getUserId());
        assertTrue(playlist.getSongs().isEmpty());
        assertEquals(1, playlist.getDomainEvents().size());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre de la playlist es nulo o vacío")
    void createPlaylistWithInvalidName() {
        UUID userId = UUID.randomUUID();
        assertThrows(InvalidPlaylistNameException.class, () -> Playlist.create("", userId));
        assertThrows(InvalidPlaylistNameException.class, () -> Playlist.create(null, userId));
    }

    @Test
    @DisplayName("Debe agregar una canción a la playlist exitosamente")
    void addSongSuccessfully() {
        UUID userId = UUID.randomUUID();
        Playlist playlist = Playlist.create("Chill Vibes", userId);
        Song song = Song.create("spotify123", "Yellow", "Coldplay", "Parachutes", 269);

        playlist.addSong(song);

        assertEquals(1, playlist.getSongs().size());
        assertEquals("Yellow", playlist.getSongs().get(0).getTitle().getValue());
    }

    @Test
    @DisplayName("Debe lanzar DuplicateSongException al intentar agregar la misma canción dos veces")
    void addDuplicateSongThrowsException() {
        UUID userId = UUID.randomUUID();
        Playlist playlist = Playlist.create("Chill Vibes", userId);
        Song song = Song.create("spotify123", "Yellow", "Coldplay", "Parachutes", 269);

        playlist.addSong(song);

        assertThrows(DuplicateSongException.class, () -> playlist.addSong(song));
    }

    @Test
    @DisplayName("Debe eliminar una canción existente de la playlist")
    void removeSongSuccessfully() {
        UUID userId = UUID.randomUUID();
        Playlist playlist = Playlist.create("Work Hits", userId);
        Song song = Song.create("spotify456", "Closer", "Chainsmokers", "Collage", 245);
        playlist.addSong(song);

        playlist.removeSong(song.getId());

        assertTrue(playlist.getSongs().isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar SongNotFoundException al intentar eliminar una canción que no pertenece a la playlist")
    void removeNonExistentSongThrowsException() {
        UUID userId = UUID.randomUUID();
        Playlist playlist = Playlist.create("Work Hits", userId);

        assertThrows(SongNotFoundException.class, () -> playlist.removeSong(UUID.randomUUID()));
    }
}
