package com.castagno.dev.incident_reports_api.security;

import com.castagno.dev.incident_reports_api.model.User;
import com.castagno.dev.incident_reports_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UserDetailsService} que Spring Security usa
 * para cargar los datos del usuario durante la autenticación.
 *
 * El método loadUserByUsername se llama automáticamente en el proceso
 * de login y también en cada request cuando se valida el JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Busca el usuario por username o email.
     * Permite hacer login con cualquiera de los dos.
     *
     * @param usernameOrEmail puede ser el username o el email del usuario
     * @throws UsernameNotFoundException si no existe el usuario o está deshabilitado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("User not found with username or email: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "User not found with username or email: " + usernameOrEmail
                    );
                });

        if (!user.getEnabled()) {
            log.warn("Disabled user attempted to login: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
        }

        log.debug("User loaded successfully: {}", user.getUsername());
        return UserDetailsImpl.build(user);
    }
}