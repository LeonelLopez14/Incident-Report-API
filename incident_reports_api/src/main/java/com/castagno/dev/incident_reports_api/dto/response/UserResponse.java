package com.castagno.dev.incident_reports_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;


@Getter
@Builder
@Schema(description = "Datos públicos de un usuario")
public class UserResponse {

    @Schema(example = "1")
    private final Long id;

    @Schema(example = "john_doe")
    private final String username;

    @Schema(example = "john@example.com")
    private final String email;

    @Schema(example = "John Doe")
    private final String fullName;

    @Schema(example = "true")
    private final Boolean enabled;

    @Schema(example = "[\"ROLE_USER\"]")
    private final Set<String> roles;

    @Schema(example = "2024-01-15T10:30:00")
    private final LocalDateTime createdAt;

    @Schema(example = "2024-01-20T14:00:00")
    private final LocalDateTime updatedAt;
}