package com.esteban.playlistapi.infrastructure.persistence.repository;

import com.esteban.playlistapi.infrastructure.persistence.entity.PlaylistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz Spring Data JPA para la entidad PlaylistJpaEntity.
 * Pertenece exclusivamente a la capa Infrastructure.
 */
@Repository
public interface SpringDataJpaPlaylistRepository extends JpaRepository<PlaylistJpaEntity, UUID> {

    List<PlaylistJpaEntity> findByUserId(UUID userId);
}
