package com.castagno.dev.incident_reports_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;


@Getter
@Builder
@Schema(description = "Estadísticas agregadas de incidentes")
public class ReportStatsResponse {

    @Schema(description = "Total de incidentes registrados", example = "150")
    private final long totalIncidents;

    @Schema(description = "Incidentes abiertos", example = "45")
    private final long openIncidents;

    @Schema(description = "Incidentes en progreso", example = "30")
    private final long inProgressIncidents;

    @Schema(description = "Incidentes resueltos", example = "60")
    private final long resolvedIncidents;

    @Schema(description = "Incidentes cerrados", example = "15")
    private final long closedIncidents;

    @Schema(description = "Incidentes por prioridad", example = "{\"HIGH\": 20, \"MEDIUM\": 50, \"LOW\": 80}")
    private final Map<String, Long> incidentsByPriority;

    @Schema(description = "Incidentes por categoría", example = "{\"Infrastructure\": 40, \"Security\": 30}")
    private final Map<String, Long> incidentsByCategory;

    @Schema(description = "Incidentes creados en los últimos 30 días", example = "25")
    private final long incidentsLast30Days;
}
