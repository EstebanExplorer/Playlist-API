package com.esteban.playlistapi.shared.exception;

/**
 * Excepción lanzada cuando los datos de una Consulta (Query DTO) son inválidos o nulos.
 * Pertenece a la jerarquía de ApplicationException.
 */
public class InvalidQueryException extends ApplicationException {

    public InvalidQueryException(String message) {
        super(message);
    }
}
