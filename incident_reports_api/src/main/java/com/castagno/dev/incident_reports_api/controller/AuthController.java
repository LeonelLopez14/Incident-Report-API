package com.castagno.dev.incident_reports_api.controller;

import com.castagno.dev.incident_reports_api.dto.request.LoginRequest;
import com.castagno.dev.incident_reports_api.dto.request.RegisterRequest;
import com.castagno.dev.incident_reports_api.dto.response.JwtResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para login, registro y renovación de tokens")
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login

    @Operation(
            summary     = "Iniciar sesión",
            description = "Autentica un usuario con username/email y password. Retorna access token y refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/register

    @Operation(
            summary     = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta. Si no se especifican roles, se asigna ROLE_USER por defecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Username o email ya registrado, o datos inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // POST /api/auth/refresh

    @Operation(
            summary     = "Renovar access token",
            description = "Genera un nuevo access token usando un refresh token válido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @RequestParam("refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
