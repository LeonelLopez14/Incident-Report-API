package com.castagno.dev.incident_reports_api.repository;

import com.castagno.dev.incident_reports_api.model.EIncidentStatus;
import com.castagno.dev.incident_reports_api.model.EPriority;
import com.castagno.dev.incident_reports_api.model.Incident;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class IncidentSpecification {

    private IncidentSpecification() {}


    public static Specification<Incident> build(
            EIncidentStatus status,
            EPriority priority,
            String keyword,
            Long userId,
            String category
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descriptionMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descriptionMatch));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("reportedBy").get("id"), userId));
            }

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
