package com.esteban.playlistapi.application.auth.exception;

import com.esteban.playlistapi.shared.exception.ApplicationException;

/**
 * Excepción lanzada cuando las credenciales de autenticación son inválidas.
 * Hereda de ApplicationException para separar los errores de orquestación de acceso de las reglas de dominio.
 */
public class InvalidCredentialsException extends ApplicationException {

    public InvalidCredentialsException() {
        super("Nombre de usuario o contraseña incorrectos.");
    }
}
