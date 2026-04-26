package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.request.LoginRequest;
import com.castagno.dev.incident_reports_api.dto.request.RegisterRequest;
import com.castagno.dev.incident_reports_api.dto.response.JwtResponse;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.exception.BadRequestException;
import com.castagno.dev.incident_reports_api.exception.ResourceNotFoundException;
import com.castagno.dev.incident_reports_api.exception.UnauthorizedException;
import com.castagno.dev.incident_reports_api.model.ERole;
import com.castagno.dev.incident_reports_api.model.Role;
import com.castagno.dev.incident_reports_api.model.User;
import com.castagno.dev.incident_reports_api.repository.RoleRepository;
import com.castagno.dev.incident_reports_api.repository.UserRepository;
import com.castagno.dev.incident_reports_api.security.JwtTokenProvider;
import com.castagno.dev.incident_reports_api.security.UserDetailsImpl;
import com.castagno.dev.incident_reports_api.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación del servicio de autenticación.
 *
 * Responsabilidades:
 * - Validar credenciales y emitir JWT
 * - Registrar nuevos usuarios con roles adecuados
 * - Renovar tokens expirados
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Login

    @Override
    public JwtResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken  = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("User '{}' logged in successfully", userDetails.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    // Register
    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(resolveRoles(request.getRoles()))
                .build();

        userRepository.save(user);
        log.info("New user registered: '{}'", user.getUsername());

        return new MessageResponse(Constants.MSG_USER_CREATED);
    }

    // Refresh token

    @Override
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!user.getEnabled()) {
            throw new UnauthorizedException("User account is disabled");
        }

        UserDetailsImpl userDetails = com.castagno.dev.incident_reports_api.security.UserDetailsImpl.build(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.debug("Token refreshed for user '{}'", username);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // mismo refresh token
                .expiresIn(86400000L)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    //  Privados

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<Role> roles = new HashSet<>();

        if (requestedRoles == null || requestedRoles.isEmpty()) {
            roles.add(findRole(ERole.ROLE_USER));
            return roles;
        }

        for (String roleName : requestedRoles) {
            try {
                ERole eRole = ERole.valueOf(roleName.toUpperCase());
                roles.add(findRole(eRole));
            } catch (IllegalArgumentException e) {
                log.warn("Role '{}' not recognized, skipping", roleName);
            }
        }

        if (roles.isEmpty()) {
            roles.add(findRole(ERole.ROLE_USER));
        }

        return roles;
    }

    private Role findRole(ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", eRole.name()));
    }
}