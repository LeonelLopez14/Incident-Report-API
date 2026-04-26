package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.request.IncidentRequest;
import com.castagno.dev.incident_reports_api.dto.request.UpdateIncidentStatusRequest;
import com.castagno.dev.incident_reports_api.dto.response.IncidentResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.exception.BadRequestException;
import com.castagno.dev.incident_reports_api.exception.ForbiddenException;
import com.castagno.dev.incident_reports_api.exception.ResourceNotFoundException;
import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import com.castagno.dev.incident_reports_api.model.Incident;
import com.castagno.dev.incident_reports_api.model.User;
import com.castagno.dev.incident_reports_api.repository.IncidentRepository;
import com.castagno.dev.incident_reports_api.repository.IncidentSpecification;
import com.castagno.dev.incident_reports_api.repository.UserRepository;
import com.castagno.dev.incident_reports_api.security.UserDetailsImpl;
import com.castagno.dev.incident_reports_api.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de incidentes.
 *
 * Reglas de negocio principales:
 * - ROLE_USER solo ve y edita sus propios incidentes
 * - ROLE_ANALYST puede ver todos, editar y cambiar estado
 * - ROLE_ADMIN tiene acceso total incluyendo eliminar
 * - El estado RESOLVED/CLOSED requiere resolutionNotes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    // Consultas

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IncidentResponse> findAll(
            int page, int size, String sortBy, String sortDir,
            EIncidentStatus status, EPriority priority,
            String category, String keyword
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);

        Long ownerFilter = isCurrentUserRole("ROLE_USER") ? getCurrentUserId() : null;

        Specification<Incident> spec = IncidentSpecification.build(
                status, priority, keyword, ownerFilter, category
        );

        Page<IncidentResponse> resultPage = incidentRepository
                .findAll(spec, pageable)
                .map(this::mapToResponse);

        return PageResponse.of(resultPage);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponse findById(Long id) {
        Incident incident = findIncidentOrThrow(id);
        checkReadAccess(incident);
        return mapToResponse(incident);
    }

    // Creación

    @Override
    @Transactional
    public IncidentResponse create(IncidentRequest request) {
        User currentUser = getCurrentUser();
        User assignedTo  = resolveAssignedUser(request.getAssignedToId());

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .location(request.getLocation())
                .status(EIncidentStatus.OPEN)
                .reportedBy(currentUser)
                .assignedTo(assignedTo)
                .build();

        Incident saved = incidentRepository.save(incident);
        log.info("Incident #{} created by user '{}'", saved.getId(), currentUser.getUsername());

        return mapToResponse(saved);
    }

    // Actualización

    @Override
    @Transactional
    public IncidentResponse update(Long id, IncidentRequest request) {
        Incident incident = findIncidentOrThrow(id);
        checkWriteAccess(incident);

        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setPriority(request.getPriority());
        incident.setCategory(request.getCategory());
        incident.setLocation(request.getLocation());

        // Solo ADMIN/ANALYST pueden reasignar
        if (request.getAssignedToId() != null && !isCurrentUserRole("ROLE_USER")) {
            incident.setAssignedTo(resolveAssignedUser(request.getAssignedToId()));
        }

        Incident saved = incidentRepository.save(incident);
        log.info("Incident #{} updated by user '{}'", saved.getId(), getCurrentUsername());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public IncidentResponse updateStatus(Long id, UpdateIncidentStatusRequest request) {
        Incident incident = findIncidentOrThrow(id);
        checkWriteAccess(incident);

        EIncidentStatus newStatus = request.getStatus();

        // ROLE_USER no puede cerrar ni rechazar
        if (isCurrentUserRole("ROLE_USER")
                && (newStatus == EIncidentStatus.CLOSED || newStatus == EIncidentStatus.REJECTED)) {
            throw new ForbiddenException("Users cannot close or reject incidents");
        }

        // Resolución/cierre requiere notas
        if ((newStatus == EIncidentStatus.RESOLVED || newStatus == EIncidentStatus.CLOSED)
                && (request.getResolutionNotes() == null || request.getResolutionNotes().isBlank())) {
            throw new BadRequestException("Resolution notes are required when resolving or closing an incident");
        }

        incident.setStatus(newStatus);

        if (request.getResolutionNotes() != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }

        // Marcar fecha de resolución
        if (newStatus == EIncidentStatus.RESOLVED || newStatus == EIncidentStatus.CLOSED) {
            incident.setResolvedAt(LocalDateTime.now());
        } else {
            incident.setResolvedAt(null); // Si se reabre, limpiar la fecha
        }

        Incident saved = incidentRepository.save(incident);
        log.info("Incident #{} status changed to '{}' by user '{}'",
                saved.getId(), newStatus, getCurrentUsername());

        return mapToResponse(saved);
    }

    // Eliminación
    @Override
    @Transactional
    public MessageResponse delete(Long id) {
        Incident incident = findIncidentOrThrow(id);
        incidentRepository.delete(incident);
        log.info("Incident #{} deleted by user '{}'", id, getCurrentUsername());
        return new MessageResponse(Constants.MSG_INCIDENT_DELETED);
    }

    // Control de acceso

    /**
     * ROLE_USER solo puede leer incidentes propios.
     */
    private void checkReadAccess(Incident incident) {
        if (isCurrentUserRole("ROLE_USER")
                && !incident.getReportedBy().getId().equals(getCurrentUserId())) {
            throw new ForbiddenException("You can only view your own incidents");
        }
    }

    /**
     * ROLE_USER solo puede modificar incidentes propios.
     * ADMIN y ANALYST pueden modificar cualquiera.
     */
    private void checkWriteAccess(Incident incident) {
        if (isCurrentUserRole("ROLE_USER")
                && !incident.getReportedBy().getId().equals(getCurrentUserId())) {
            throw new ForbiddenException("You can only modify your own incidents");
        }
    }

    // Helpers privados

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
    }

    private User resolveAssignedUser(Long assignedToId) {
        if (assignedToId == null) return null;
        return userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedToId));
    }

    private User getCurrentUser() {
        return userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", getCurrentUsername()));
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) auth.getPrincipal();
    }

    private Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    private String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }

    /**
     * Verifica si el usuario autenticado tiene EXACTAMENTE el rol indicado
     */
    private boolean isCurrentUserRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }

    /**
     * Mapea la entidad Incident a su DTO de respuesta.
     */
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
