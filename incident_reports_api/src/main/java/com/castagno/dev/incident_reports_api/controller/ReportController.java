package com.castagno.dev.incident_reports_api.controller;

import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.ReportStatsResponse;
import com.castagno.dev.incident_reports_api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de reportes y estadísticas.
 * Todos los endpoints requieren ROLE_ADMIN o ROLE_ANALYST.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
@Tag(name = "Reports", description = "Estadísticas y reportes del sistema de incidentes")
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/stats

    @Operation(
            summary     = "Estadísticas generales",
            description = "Retorna totales por estado, prioridad, categoría y actividad reciente. "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas generadas exitosamente",
                    content = @Content(schema = @Schema(implementation = ReportStatsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere ROLE_ADMIN o ROLE_ANALYST")
    })
    @GetMapping("/stats")
    public ResponseEntity<ReportStatsResponse> getStats() {
        return ResponseEntity.ok(reportService.getStats());
    }

    //  GET /api/reports/critical

    @Operation(
            summary     = "Incidentes críticos activos",
            description = "Lista los incidentes con prioridad CRITICAL en estado OPEN o IN_PROGRESS, "
                    + "ordenados por fecha de creación ascendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Requiere ROLE_ADMIN o ROLE_ANALYST")
    })
    @GetMapping("/critical")
    public ResponseEntity<PageResponse<IncidentResponse>> getCriticalActive(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reportService.getCriticalActive(page, size));
    }

    // GET /api/reports/recent 

    @Operation(
            summary     = "Incidentes recientes",
            description = "Lista los incidentes creados en los últimos N días, ordenados por fecha descendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetro 'days' inválido"),
            @ApiResponse(responseCode = "403", description = "Requiere ROLE_ADMIN o ROLE_ANALYST")
    })
    @GetMapping("/recent")
    public ResponseEntity<PageResponse<IncidentResponse>> getRecentIncidents(
            @Parameter(description = "Número de días hacia atrás", example = "30")
            @RequestParam(defaultValue = "30") int days,

            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reportService.getRecentIncidents(days, page, size));
    }
}