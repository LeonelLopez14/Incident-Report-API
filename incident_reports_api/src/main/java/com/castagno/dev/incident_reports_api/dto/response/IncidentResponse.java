package com.castagno.dev.incident_reports_api.dto.response;

import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
@Schema(description = "Datos completos de un incidente")
public class IncidentResponse {

    @Schema(example = "1")
    private final Long  id;

    @Schema(example = "Server down in production")
    private final String title;

    @Schema(example = "The main production server stopped responding at 10:30 AM.")
    private final String  description;

    @Schema(example = "OPEN")
    private final EIncidentStatus status;

    @Schema(example = "HIGH")
    private final EPriority priority;

    @Schema(example = "Infrastructure")
    private final String category;

    @Schema(example = "Data Center - Rack 3")
    private final String location;

    @Schema(description = "Usuario que reportó el incidente")
    private final UserSummary reportedBy;

    @Schema(description = "Usuario asignado para resolver (puede ser null)")
    private final UserSummary assignedTo;

    @Schema(example = "Issue fixed by restarting the service.")
    private final String resolutionNotes;

    @Schema(example = "2024-01-15T12:00:00")
    private final LocalDateTime resolvedAt;

    @Schema(example = "2024-01-15T10:30:00")
    private final LocalDateTime createdAt;

    @Schema(example = "2024-01-15T11:00:00")
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    @Schema(description = "Resumen de usuario")
    public static class UserSummary {
        private final Long   id;
        private final String username;
        private final String fullName;
    }
}
