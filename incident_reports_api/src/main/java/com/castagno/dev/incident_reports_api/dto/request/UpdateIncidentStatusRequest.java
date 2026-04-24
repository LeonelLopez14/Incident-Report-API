package com.castagno.dev.incident_reports_api.dto.request;

import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Schema(description = "Actualización de estado de un incidente")
public class UpdateIncidentStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Nuevo estado del incidente", example = "IN_PROGRESS")
    private EIncidentStatus status;

    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    @Schema(description = "Notas de resolución (requeridas al cerrar o resolver)", example = "Issue fixed by restarting the service.")
    private String resolutionNotes;
}
