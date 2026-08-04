package com.esteban.playlistapi.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO HTTP Request para el inicio de sesión de usuarios (UC-001).
 * Implementado como un Record Java 21 inmutable con validación declarativa.
 */
public record LoginRequest(
        @NotBlank(message = "El correo electrónico no puede estar vacío.")
        @Email(message = "El correo electrónico debe ser una dirección de email válida.")
        String email,

        @NotBlank(message = "La contraseña no puede estar vacía.")
        @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres.")
        String password
) {}
