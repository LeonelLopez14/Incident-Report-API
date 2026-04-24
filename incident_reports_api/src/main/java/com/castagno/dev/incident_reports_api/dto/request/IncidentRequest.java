package com.castagno.dev.incident_reports_api.dto.request;

import com.castagno.dev.incident_reports_api.model.EPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar un incidente")
public class IncidentRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    @Schema(description = "Título descriptivo del incidente", example = "Server down in production")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Schema(description = "Descripción detallada del incidente", example = "The main production server stopped responding at 10:30 AM.")
    private String description;

    @NotNull(message = "Priority is required")
    @Schema(description = "Nivel de prioridad", example = "HIGH")
    private EPriority priority;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Schema(description = "Categoría del incidente", example = "Infrastructure")
    private String category;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(description = "Ubicación física o lógica del incidente", example = "Data Center - Rack 3")
    private String location;

    /**
     * ID del usuario al que se asigna el incidente.
     * Solo ADMIN y ANALYST pueden asignar.
     */
    @Schema(description = "ID del usuario asignado para resolver el incidente", example = "2")
    private Long assignedToId;
}
