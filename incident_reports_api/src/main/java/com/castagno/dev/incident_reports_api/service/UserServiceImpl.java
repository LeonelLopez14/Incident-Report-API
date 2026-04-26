package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.request.UpdateUserRequest;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.UserResponse;
import com.castagno.dev.incident_reports_api.exception.BadRequestException;
import com.castagno.dev.incident_reports_api.exception.ResourceNotFoundException;
import com.castagno.dev.incident_reports_api.model.ERole;
import com.castagno.dev.incident_reports_api.model.Role;
import com.castagno.dev.incident_reports_api.model.User;
import com.castagno.dev.incident_reports_api.repository.RoleRepository;
import com.castagno.dev.incident_reports_api.repository.UserRepository;
import com.castagno.dev.incident_reports_api.security.UserDetailsImpl;
import com.castagno.dev.incident_reports_api.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Consultas

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE), sort);
        Page<UserResponse> resultPage = userRepository.findAll(pageable).map(this::mapToResponse);

        return PageResponse.of(resultPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = findUserOrThrow(id);
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        UserDetailsImpl currentUser = getCurrentUserDetails();
        User user = findUserOrThrow(currentUser.getId());
        return mapToResponse(user);
    }

    //  Modificaciones

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);


        if (StringUtils.hasText(request.getUsername())
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        User saved = userRepository.save(user);
        log.info("User '{}' updated", saved.getUsername());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public MessageResponse disable(Long id) {
        User user = findUserOrThrow(id);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User '{}' disabled", user.getUsername());
        return new MessageResponse("User '" + user.getUsername() + "' has been disabled");
    }

    @Override
    @Transactional
    public MessageResponse delete(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        log.info("User '{}' permanently deleted", user.getUsername());
        return new MessageResponse(Constants.MSG_USER_DELETED);
    }

    //  Helpers privados

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) auth.getPrincipal();
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            try {
                ERole eRole = ERole.valueOf(name.toUpperCase());
                Role role = roleRepository.findByName(eRole)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
                roles.add(role);
            } catch (IllegalArgumentException e) {
                log.warn("Role '{}' not recognized, skipping", name);
            }
        }
        if (roles.isEmpty()) {
            roles.add(roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER")));
        }
        return roles;
    }

    /**
     * Convierte una entidad User a su DTO de respuesta.
     * Mapea los roles a un Set de Strings con los nombres de los roles.
     */
    private UserResponse mapToResponse(User user) {
        Set<String> roles = new HashSet<>();
        user.getRoles().forEach(role -> roles.add(role.getName().name()));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.getEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
