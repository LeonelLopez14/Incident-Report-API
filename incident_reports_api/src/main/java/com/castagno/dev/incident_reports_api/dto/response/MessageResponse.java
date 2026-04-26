package com.castagno.dev.incident_reports_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Schema(description = "Respuesta con mensaje informativo")
public class MessageResponse {

    @Schema(example = "Operation completed successfully")
    private final String message;
}
