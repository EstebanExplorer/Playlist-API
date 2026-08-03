package com.esteban.playlistapi.domain.repository;

import com.esteban.playlistapi.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Output Port) que define el contrato de persistencia para Usuarios.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
