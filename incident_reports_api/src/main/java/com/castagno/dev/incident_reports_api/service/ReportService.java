package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.ReportStatsResponse;

/**
 * Accesible solo para ROLE_ADMIN y ROLE_ANALYST.
 */
public interface ReportService {

    /**
     * Retorna estadísticas agregadas del sistema de incidentes.
     */
    ReportStatsResponse getStats();

    /**
     * Lista incidentes críticos activos (OPEN o IN_PROGRESS con prioridad CRITICAL).
     */
    PageResponse<IncidentResponse> getCriticalActive(int page, int size);

    /**
     * Lista incidentes creados en los últimos N días.
     */
    PageResponse<IncidentResponse> getRecentIncidents(int days, int page, int size);
}