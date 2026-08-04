package com.esteban.playlistapi.infrastructure.security.adapter;

import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.domain.repository.UserRepository;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de Persistencia en Memoria para el puerto UserRepository del Dominio.
 * Pertenece exclusivamente a la capa Infrastructure.
 */
@Component
public class InMemoryUserRepositoryAdapter implements UserRepository {

    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo.");
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo.");
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return users.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
