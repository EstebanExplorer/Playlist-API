package com.esteban.playlistapi.infrastructure.security.adapter;

import com.esteban.playlistapi.application.auth.port.PasswordEncoderPort;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adaptador de Seguridad que satisface el puerto PasswordEncoderPort.
 * Utiliza Spring Security BCryptPasswordEncoder para codificar y verificar contraseñas.
 * Pertenece exclusivamente a la capa Infrastructure.
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder no puede ser nulo");
    }

    @Override
    public String encode(String rawPassword) {
        Objects.requireNonNull(rawPassword, "La contraseña en texto plano no puede ser nula para codificar.");
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        Objects.requireNonNull(rawPassword, "La contraseña en texto plano no puede ser nula para verificar.");
        Objects.requireNonNull(encodedPassword, "La contraseña codificada no puede ser nula para verificar.");
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
