package com.castagno.dev.incident_reports_api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

//Estructura unificada para TODAS las respuestas de error de la API.

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime         timestamp;
    private final int                   status;
    private final String                error;
    private final String                message;
    private final String                path;

    /**
     * Solo presente en errores de validación (@Valid).
     * Clave: nombre del campo | Valor: mensaje de error
     */
    private final Map<String, String>   validationErrors;
}
