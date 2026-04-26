package com.castagno.dev.incident_reports_api.repository;

import com.castagno.dev.incident_reports_api.model.ERole;
import com.castagno.dev.incident_reports_api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
