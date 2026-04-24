package com.castagno.dev.incident_reports_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
@Schema(description = "Respuesta de autenticación exitosa")
public class JwtResponse {

    @Schema(description = "Access token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String accessToken;

    @Schema(description = "Refresh token para renovar el access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private final String refreshToken;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private final String tokenType = "Bearer";

    @Schema(description = "Tiempo de expiración en milisegundos", example = "86400000")
    private final Long expiresIn;

    @Schema(description = "ID del usuario autenticado", example = "1")
    private final Long userId;

    @Schema(description = "Username del usuario autenticado", example = "admin")
    private final String username;

    @Schema(description = "Email del usuario autenticado", example = "admin@incidentreport.com")
    private final String email;

    @Schema(description = "Roles asignados al usuario", example = "[\"ROLE_ADMIN\"]")
    private final List<String> roles;
}
