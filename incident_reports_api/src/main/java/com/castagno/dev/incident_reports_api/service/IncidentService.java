package com.castagno.dev.incident_reports_api.service;


import com.castagno.dev.incident_reports_api.dto.request.IncidentRequest;
import com.castagno.dev.incident_reports_api.dto.request.UpdateIncidentStatusRequest;
import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;


public interface IncidentService {

    /**
     * Lista todos los incidentes pagados y filtrados por estado, prioridad, categoría o palabra clave.
     * ADMIN y ANALYST ven todos; ROLE_USER solo los propios.
     */
    PageResponse<IncidentResponse> findAll(
            int page, int size, String sortBy, String sortDir,
            EIncidentStatus status, EPriority priority,
            String category, String keyword
    );

    /**
     * Retorna un incidente por su ID.
     */
    IncidentResponse findById(Long id);

    /**
     * Crea un nuevo incidente asociado al usuario autenticado.
     */
    IncidentResponse create(IncidentRequest request);

    /**
     * Actualiza título, descripción, prioridad, categoría y asignación.
     * ROLE_USER solo puede editar sus propios incidentes.
     */
    IncidentResponse update(Long id, IncidentRequest request);

    /**
     * Cambia el estado de un incidente a OPEN, IN_PROGRESS, RESOLVED o REJECTED.
     * ROLE_USER no puede cerrar ni rechazar.
     */
    IncidentResponse updateStatus(Long id, UpdateIncidentStatusRequest request);

    /**
     * Elimina un incidente. Solo ADMIN.
     */
    MessageResponse delete(Long id);
}
