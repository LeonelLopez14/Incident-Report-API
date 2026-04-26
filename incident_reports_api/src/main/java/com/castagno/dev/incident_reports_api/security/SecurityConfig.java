package com.castagno.dev.incident_reports_api.security;

import com.castagno.dev.incident_reports_api.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl  userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint       jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler  jwtAccessDeniedHandler;
    private final PasswordEncoder         passwordEncoder;

    // ── Filter Chain ──────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (API REST stateless no lo necesita)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Manejo de excepciones de seguridad
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // Sin sesión HTTP — cada request se autentica via JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (login, register, swagger, actuator)
                        .requestMatchers(Constants.PUBLIC_ENDPOINTS).permitAll()

                        // Solo ADMIN puede gestionar usuarios
                        .requestMatchers(HttpMethod.GET,    "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // Reportes: ADMIN y ANALYST
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ANALYST")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Registrar nuestro filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Authentication Provider ───────────────────────────────

    /**
     * Configura el proveedor de autenticación y el PasswordEncoder BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ── CORS ──────────────────────────────────────────────────

    /**
     * Configuración CORS para permitir requests desde el frontend.
     * En producción se debe restringir allowedOrigins al dominio real.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // En dev permitimos todos los orígenes; en prod usar el dominio real
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
