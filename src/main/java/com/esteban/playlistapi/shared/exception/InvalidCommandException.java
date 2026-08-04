package com.esteban.playlistapi.shared.exception;

/**
 * Excepción lanzada cuando los datos de un Comando (Command DTO) son inválidos o nulos.
 * Pertenece a la jerarquía de ApplicationException.
 */
public class InvalidCommandException extends ApplicationException {

    public InvalidCommandException(String message) {
        super(message);
    }
}
