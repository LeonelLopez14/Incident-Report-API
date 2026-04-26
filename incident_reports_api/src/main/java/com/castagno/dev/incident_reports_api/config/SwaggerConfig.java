package com.castagno.dev.incident_reports_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI 3.

 * Acceso: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme())
                );
    }

    private Info buildInfo() {
        return new Info()
                .title("Incident Report API")
                .description("""
                        REST API para la gestión de reportes de incidentes.
                        
                        **Autenticación:** usar el endpoint `/api/auth/login` para obtener
                        el token JWT y luego hacer clic en **Authorize** (arriba a la derecha)
                        para incluirlo en las peticiones.
                        
                        **Roles disponibles:**
                        - `ROLE_ADMIN` — acceso total
                        - `ROLE_ANALYST` — gestión de incidentes y reportes
                        - `ROLE_USER` — solo sus propios incidentes
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Incident Report Team")
                        .email("test@incidentreport.com")
                );
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingresar el token JWT obtenido en /api/auth/login. Formato: Bearer <token>");
    }
}