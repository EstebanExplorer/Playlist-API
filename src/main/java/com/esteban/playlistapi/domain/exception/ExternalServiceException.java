package com.esteban.playlistapi.domain.exception;

public class ExternalServiceException extends DomainException {
    public ExternalServiceException(String serviceName, String message) {
        super("Fallo en el servicio externo '" + serviceName + "': " + message);
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("Fallo en el servicio externo '" + serviceName + "': " + message, cause);
    }
}
