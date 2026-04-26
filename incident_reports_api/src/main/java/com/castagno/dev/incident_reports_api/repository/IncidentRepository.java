package com.castagno.dev.incident_reports_api.repository;

import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import com.castagno.dev.incident_reports_api.model.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>,
        JpaSpecificationExecutor<Incident> {

    // Consultas por usuario

    Page<Incident> findByReportedById(Long userId, Pageable pageable);

    Page<Incident> findByAssignedToId(Long userId, Pageable pageable);

    // Consultas por estado y prioridad

    Page<Incident> findByStatus(EIncidentStatus status, Pageable pageable);

    Page<Incident> findByPriority(EPriority priority, Pageable pageable);

    Page<Incident> findByStatusAndPriority(EIncidentStatus status, EPriority priority, Pageable pageable);

    //  Búsqueda por texto en título o descripción

    @Query("""
            SELECT i FROM Incident i
            WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Incident> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    //  Consultas para reportes y estadísticas

    long countByStatus(EIncidentStatus status);

    long countByPriority(EPriority priority);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countGroupByStatus();


    @Query("SELECT i.priority, COUNT(i) FROM Incident i GROUP BY i.priority")
    List<Object[]> countGroupByPriority();


    @Query("SELECT i.category, COUNT(i) FROM Incident i WHERE i.category IS NOT NULL GROUP BY i.category")
    List<Object[]> countGroupByCategory();

    @Query("""
            SELECT i FROM Incident i
            WHERE i.priority = 'CRITICAL'
              AND i.status IN ('OPEN', 'IN_PROGRESS')
            ORDER BY i.createdAt ASC
            """)
    List<Incident> findCriticalActiveIncidents();
}
