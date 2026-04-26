package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.ReportStatsResponse;
import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import com.castagno.dev.incident_reports_api.model.Incident;
import com.castagno.dev.incident_reports_api.repository.IncidentRepository;
import com.castagno.dev.incident_reports_api.repository.IncidentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de reportes.
 * Agrega datos del repositorio para construir estadísticas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final IncidentRepository incidentRepository;

    // Estadísticas

    @Override
    @Transactional(readOnly = true)
    public ReportStatsResponse getStats() {
        long total      = incidentRepository.count();
        long open       = incidentRepository.countByStatus(EIncidentStatus.OPEN);
        long inProgress = incidentRepository.countByStatus(EIncidentStatus.IN_PROGRESS);
        long resolved   = incidentRepository.countByStatus(EIncidentStatus.RESOLVED);
        long closed     = incidentRepository.countByStatus(EIncidentStatus.CLOSED);
        long last30Days = incidentRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30));

        Map<String, Long> byPriority = buildMapFromObjectArray(
                incidentRepository.countGroupByPriority()
        );

        Map<String, Long> byCategory = buildMapFromObjectArray(
                incidentRepository.countGroupByCategory()
        );

        log.debug("Report stats generated: total={}, open={}, inProgress={}", total, open, inProgress);

        return ReportStatsResponse.builder()
                .totalIncidents(total)
                .openIncidents(open)
                .inProgressIncidents(inProgress)
                .resolvedIncidents(resolved)
                .closedIncidents(closed)
                .incidentsByPriority(byPriority)
                .incidentsByCategory(byCategory)
                .incidentsLast30Days(last30Days)
                .build();
    }

    // Listados especiales

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getCriticalActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Specification<Incident> spec = IncidentSpecification.build(
                EIncidentStatus.OPEN, EPriority.CRITICAL, null, null, null
        );

        Page<IncidentResponse> result = incidentRepository
                .findAll(spec, pageable)
                .map(this::mapToResponse);

        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> getRecentIncidents(int days, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        Specification<Incident> spec = (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), since);

        Page<IncidentResponse> result = incidentRepository
                .findAll(spec, pageable)
                .map(this::mapToResponse);

        return PageResponse.of(result);
    }

    // Helpers privados

    /**
     * Convierte el resultado de una query GROUP BY (List<Object[]>) en un Map<String, Long>.
     */
    private Map<String, Long> buildMapFromObjectArray(List<Object[]> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            String key   = row[0] != null ? row[0].toString() : "UNCATEGORIZED";
            Long   count = ((Number) row[1]).longValue();
            map.put(key, count);
        }
        return map;
    }

    private IncidentResponse mapToResponse(Incident incident) {
        IncidentResponse.UserSummary reportedBy = IncidentResponse.UserSummary.builder()
                .id(incident.getReportedBy().getId())
                .username(incident.getReportedBy().getUsername())
                .fullName(incident.getReportedBy().getFullName())
                .build();

        IncidentResponse.UserSummary assignedTo = null;
        if (incident.getAssignedTo() != null) {
            assignedTo = IncidentResponse.UserSummary.builder()
                    .id(incident.getAssignedTo().getId())
                    .username(incident.getAssignedTo().getUsername())
                    .fullName(incident.getAssignedTo().getFullName())
                    .build();
        }

        return IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .status(incident.getStatus())
                .priority(incident.getPriority())
                .category(incident.getCategory())
                .location(incident.getLocation())
                .reportedBy(reportedBy)
                .assignedTo(assignedTo)
                .resolutionNotes(incident.getResolutionNotes())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
