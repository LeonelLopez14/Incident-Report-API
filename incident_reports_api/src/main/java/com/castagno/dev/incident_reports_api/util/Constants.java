package com.castagno.dev.incident_reports_api.util;

public final class Constants {

    private Constants() {
        // Clase de utilidad — no instanciar
    }

    //  JWT
    public static final String TOKEN_PREFIX      = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    //Roles
    public static final String ROLE_ADMIN    = "ROLE_ADMIN";
    public static final String ROLE_ANALYST  = "ROLE_ANALYST";
    public static final String ROLE_USER     = "ROLE_USER";

    // Paginacion
    public static final int    DEFAULT_PAGE_SIZE  = 10;
    public static final int    MAX_PAGE_SIZE      = 100;
    public static final String DEFAULT_SORT_BY    = "createdAt";
    public static final String DEFAULT_SORT_DIR   = "desc";

    // Mensajes de respuesta comunes
    public static final String MSG_INCIDENT_CREATED  = "Incident created successfully";
    public static final String MSG_INCIDENT_UPDATED  = "Incident updated successfully";
    public static final String MSG_INCIDENT_DELETED  = "Incident deleted successfully";
    public static final String MSG_USER_CREATED      = "User registered successfully";
    public static final String MSG_USER_UPDATED      = "User updated successfully";
    public static final String MSG_USER_DELETED      = "User deleted successfully";

    // Endpoints publicos
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };
}
