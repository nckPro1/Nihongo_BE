package org.example.nihongobackend.service.admin;

import org.example.nihongobackend.dto.response.admin.AdminUserPageResponse;

import java.util.UUID;

public interface AdminUserManagementService {

    AdminUserPageResponse listUsers(int page, int size);

    void setUserActive(UUID userId, boolean active);
}
