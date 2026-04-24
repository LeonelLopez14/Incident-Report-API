package com.castagno.dev.incident_reports_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;


@Getter
@NoArgsConstructor
@Schema(description = "Datos para registrar un nuevo usuario")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    @Schema(description = "Nombre de usuario único", example = "john_doe")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Email único del usuario", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#])[A-Za-z\\d@$!%*?&_#]{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one digit and one special character"
    )
    @Schema(description = "Contraseña segura", example = "Admin1234!")
    private String password;

    @Size(max = 80, message = "Full name must not exceed 80 characters")
    @Schema(description = "Nombre completo del usuario", example = "John Doe")
    private String fullName;

    /**
     * Roles a asignar. Solo ADMIN puede asignar roles distintos a ROLE_USER.*/
    @Schema(description = "Roles a asignar (solo ADMIN puede asignar ROLE_ADMIN/ROLE_ANALYST)",
            example = "[\"ROLE_USER\"]")
    private Set<String> roles;
}