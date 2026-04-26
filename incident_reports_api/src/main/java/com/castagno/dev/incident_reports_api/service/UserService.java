package com.castagno.dev.incident_reports_api.service;

import com.castagno.dev.incident_reports_api.dto.request.UpdateUserRequest;
import com.castagno.dev.incident_reports_api.dto.response.MessageResponse;
import com.castagno.dev.incident_reports_api.dto.response.PageResponse;
import com.castagno.dev.incident_reports_api.dto.response.UserResponse;


public interface UserService {

    /**
     * Retorna todos los usuarios paginados. Solo ADMIN.
     */
    PageResponse<UserResponse> findAll(int page, int size, String sortBy, String sortDir);

    /**
     * Busca un usuario por su ID.
     */
    UserResponse findById(Long id);

    /**
     * Retorna el perfil del usuario autenticado actualmente.
     */
    UserResponse getMyProfile();

    /**
     * Actualiza los datos de un usuario. Solo ADMIN.
     */
    UserResponse update(Long id, UpdateUserRequest request);

    /**
     * Deshabilita un usuario. Solo ADMIN.
     */
    MessageResponse disable(Long id);

    /**
     * Elimina permanentemente un usuario. Solo ADMIN.
     */
    MessageResponse delete(Long id);
}
