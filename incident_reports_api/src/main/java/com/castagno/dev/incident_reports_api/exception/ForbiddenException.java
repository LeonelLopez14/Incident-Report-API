package com.castagno.dev.incident_reports_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando un usuario autenticado intenta acceder a un recurso
 * que no le pertenece o para el que no tiene permisos suficientes*/

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
