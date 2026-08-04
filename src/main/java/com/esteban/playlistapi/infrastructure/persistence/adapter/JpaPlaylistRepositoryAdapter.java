package com.esteban.playlistapi.infrastructure.persistence.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.infrastructure.persistence.entity.PlaylistJpaEntity;
import com.esteban.playlistapi.infrastructure.persistence.mapper.PlaylistPersistenceMapper;
import com.esteban.playlistapi.infrastructure.persistence.repository.SpringDataJpaPlaylistRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de Persistencia JPA que implementa el puerto de salida del dominio PlaylistRepository.
 * Pertenece exclusivamente a la capa Infrastructure. Desacopla Spring Data JPA de las capas internas.
 */
@Component
public class JpaPlaylistRepositoryAdapter implements PlaylistRepository {

    private final SpringDataJpaPlaylistRepository jpaRepository;
    private final PlaylistPersistenceMapper mapper;

    public JpaPlaylistRepositoryAdapter(SpringDataJpaPlaylistRepository jpaRepository,
                                        PlaylistPersistenceMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository no puede ser nulo");
        this.mapper = Objects.requireNonNull(mapper, "mapper no puede ser nulo");
    }

    @Override
    @Transactional
    public Playlist save(Playlist playlist) {
        Objects.requireNonNull(playlist, "El Agregado Playlist no puede ser nulo para persistir.");
        PlaylistJpaEntity entity = mapper.toEntity(playlist);
        PlaylistJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Playlist> findById(UUID id) {
        Objects.requireNonNull(id, "El ID de la playlist no puede ser nulo para consultar.");
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, "El ID del usuario no puede ser nulo para consultar sus playlists.");
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Playlist playlist) {
        Objects.requireNonNull(playlist, "El Agregado Playlist no puede ser nulo para eliminar.");
        PlaylistJpaEntity entity = mapper.toEntity(playlist);
        jpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Objects.requireNonNull(id, "El ID de la playlist no puede ser nulo para eliminar.");
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        Objects.requireNonNull(id, "El ID de la playlist no puede ser nulo para verificar su existencia.");
        return jpaRepository.existsById(id);
    }
}
