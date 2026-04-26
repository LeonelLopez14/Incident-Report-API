package com.castagno.dev.incident_reports_api.controller;

import com.castagno.dev.incident_reports_api.dto.request.UpdateUserRequest;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.UserResponse;
import com.castagno.dev.incident_reports_api.service.UserService;
import com.castagno.dev.incident_reports_api.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de gestión de usuarios.
 * Acceso:
 *  - GET /me → cualquier usuario autenticado
 *  - GET, PUT, DELETE → solo ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Gestión de usuarios del sistema")
public class UserController {

    private final UserService userService;

    //  GET /api/users/me

    @Operation(
            summary     = "Obtener mi perfil",
            description = "Retorna los datos del usuario autenticado actualmente."
    )
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // GET /api/users

    @Operation(
            summary     = "Listar todos los usuarios",
            description = "Retorna todos los usuarios paginados. Solo ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede listar usuarios")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @Parameter(description = "Número de página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo de ordenamiento", example = "createdAt")
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_BY) String sortBy,

            @Parameter(description = "Dirección: asc o desc", example = "desc")
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String sortDir
    ) {
        return ResponseEntity.ok(userService.findAll(page, size, sortBy, sortDir));
    }

    // GET /api/users/{id}

    @Operation(summary = "Obtener usuario por ID", description = "Solo ROLE_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede ver otros usuarios"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // PUT /api/users/{id}

    @Operation(
            summary     = "Actualizar usuario",
            description = "Actualiza username, email, nombre, estado y roles. Solo ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o username/email duplicado"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede actualizar usuarios"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // PATCH /api/users/{id}/disable

    @Operation(
            summary     = "Deshabilitar usuario",
            description = "Desactiva la cuenta sin eliminarla. Solo ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario deshabilitado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede deshabilitar usuarios"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> disable(
            @Parameter(description = "ID del usuario", example = "2")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.disable(id));
    }

    // DELETE /api/users/{id}

    @Operation(
            summary     = "Eliminar usuario permanentemente",
            description = "Elimina el usuario y todos sus datos asociados. Solo ROLE_ADMIN. Irreversible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario eliminado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar usuarios"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID del usuario", example = "2")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.delete(id));
    }
}
