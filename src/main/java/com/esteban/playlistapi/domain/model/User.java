package com.esteban.playlistapi.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del dominio que representa un Usuario de la aplicación.
 * Posee identidad propia (UUID).
 */
public class User {

    private final UUID id;
    private String username;
    private String email;
    private String password; // Hash de contraseña

    public User(UUID id, String username, String email, String password) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío.");
        }
        this.id = id;
        this.username = username.trim();
        this.email = email.trim();
        this.password = password;
    }

    public static User create(String username, String email, String password) {
        return new User(UUID.randomUUID(), username, email, password);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void updatePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        }
        this.password = newPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
