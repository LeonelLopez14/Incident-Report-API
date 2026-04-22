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


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

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