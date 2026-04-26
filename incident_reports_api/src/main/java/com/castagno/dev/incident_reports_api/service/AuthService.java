package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.request.LoginRequest;
import com.castagno.dev.incident_reports_api.dto.request.RegisterRequest;
import com.castagno.dev.incident_reports_api.dto.response.JwtResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;

public interface AuthService {

    //Autentica un usuario y genera los tokens JWT
    JwtResponse login(LoginRequest loginRequest);

    // Registra un nuevo usuario en el sistema. ROLE_USER por defecto
    MessageResponse register(RegisterRequest registerRequest);

    // Renueva el access token usando un refresh token válido.
    JwtResponse refreshToken(String refreshToken);
}
