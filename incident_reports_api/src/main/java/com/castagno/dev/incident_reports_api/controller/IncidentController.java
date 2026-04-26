package com.castagno.dev.incident_reports_api.controller;

import com.castagno.dev.incident_reports_api.dto.request.IncidentRequest;
import com.castagno.dev.incident_reports_api.dto.request.UpdateIncidentStatusRequest;
import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import com.castagno.dev.incident_reports_api.service.IncidentService;
import com.castagno.dev.incident_reports_api.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de incidentes.
 * Requiere autenticación en todos los endpoints.
 *
 * Reglas de acceso:
 *  - GET (listar/buscar): todos los roles autenticados
 *  - POST (crear): todos los roles autenticados
 *  - PUT (editar): todos los roles, pero ROLE_USER solo los propios
 *  - DELETE: solo ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Incidents", description = "Gestión del ciclo de vida de incidentes")
public class IncidentController {

    private final IncidentService incidentService;

    //  GET /api/incidents

    @Operation(
            summary     = "Listar incidentes",
            description = "Retorna incidentes paginados con filtros opcionales. "
                    + "ROLE_USER solo ve los propios; ADMIN y ANALYST ven todos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<PageResponse<IncidentResponse>> findAll(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo de ordenamiento", example = "createdAt")
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Dirección de orden: asc o desc", example = "desc")
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String sortDir,

            @Parameter(description = "Filtrar por estado", example = "OPEN")
            @RequestParam(required = false) EIncidentStatus status,

            @Parameter(description = "Filtrar por prioridad", example = "HIGH")
            @RequestParam(required = false) EPriority priority,

            @Parameter(description = "Filtrar por categoría", example = "Infrastructure")
            @RequestParam(required = false) String category,

            @Parameter(description = "Búsqueda por texto en título o descripción")
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                incidentService.findAll(page, size, sortBy, sortDir, status, priority, category, keyword)
        );
    }

    // GET /api/incidents/{id}

    @Operation(summary = "Obtener incidente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidente encontrado",
                    content = @Content(schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "403", description = "Sin permisos para ver este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> findById(
            @Parameter(description = "ID del incidente", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(incidentService.findById(id));
    }

    // POST /api/incidents

    @Operation(
            summary     = "Crear incidente",
            description = "Crea un nuevo incidente asociado al usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incidente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<IncidentResponse> create(
            @Valid @RequestBody IncidentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    // PUT /api/incidents/{id}

    @Operation(
            summary     = "Actualizar incidente",
            description = "Actualiza los datos del incidente. ROLE_USER solo puede editar los propios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidente actualizado",
                    content = @Content(schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para editar este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(
            @Parameter(description = "ID del incidente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody IncidentRequest request
    ) {
        return ResponseEntity.ok(incidentService.update(id, request));
    }

    // PATCH /api/incidents/{id}/status

    @Operation(
            summary     = "Cambiar estado del incidente",
            description = "Actualiza el estado del incidente. "
                    + "ROLE_USER no puede usar CLOSED ni REJECTED. "
                    + "RESOLVED y CLOSED requieren resolutionNotes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = IncidentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Transición inválida o notas faltantes"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para este cambio de estado"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @Parameter(description = "ID del incidente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request
    ) {
        return ResponseEntity.ok(incidentService.updateStatus(id, request));
    }

    // DELETE /api/incidents/{id}

    @Operation(
            summary     = "Eliminar incidente",
            description = "Elimina permanentemente un incidente. Solo ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidente eliminado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar incidentes"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID del incidente", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(incidentService.delete(id));
    }
}
