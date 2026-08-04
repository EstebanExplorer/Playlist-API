package com.esteban.playlistapi.domain.repository;

import com.esteban.playlistapi.domain.model.Playlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Output Port) que define el contrato de persistencia para las Playlists.
 * Implementado por adaptadores en la capa de infraestructura.
 */
public interface PlaylistRepository {

    Playlist save(Playlist playlist);

    Optional<Playlist> findById(UUID id);

    List<Playlist> findByUserId(UUID userId);

    void delete(Playlist playlist);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
