package com.esteban.playlistapi.shared.exception;

/**
 * Excepción base para los errores propios de la capa de Aplicación (orquestación, credenciales, flujo).
 * Hereda directamente de RuntimeException para mantener el aislamiento con la capa Domain.
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
