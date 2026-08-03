package com.esteban.playlistapi.domain.exception;

/**
 * Excepción base para todos los errores generados dentro del Dominio de negocio.
 * Permite capturar y clasificar cualquier violación de invariante de dominio.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
