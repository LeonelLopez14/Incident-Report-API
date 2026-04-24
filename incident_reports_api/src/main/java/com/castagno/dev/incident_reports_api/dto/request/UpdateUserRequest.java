package com.castagno.dev.incident_reports_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Payload para actualizar datos de un usuario.
 * Endpoint: PUT /api/users/{id}
 *
 * Todos los campos son opcionales — solo se actualiza lo que viene en el request.
 */
@Getter
@NoArgsConstructor
@Schema(description = "Datos a actualizar del usuario (todos los campos son opcionales)")
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    @Schema(description = "Nuevo username", example = "john_updated")
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Nuevo email", example = "john_new@example.com")
    private String email;

    @Size(max = 80, message = "Full name must not exceed 80 characters")
    @Schema(description = "Nombre completo", example = "John Updated Doe")
    private String fullName;

    @Schema(description = "Estado de la cuenta", example = "true")
    private Boolean enabled;

    @Schema(description = "Nuevos roles a asignar", example = "[\"ROLE_ANALYST\"]")
    private Set<String> roles;
}