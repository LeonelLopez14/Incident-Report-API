package com.castagno.dev.incident_reports_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "Credenciales de acceso al sistema")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username o email del usuario", example = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "Contraseña del usuario", example = "Admin1234!")
    private String password;
}
