package org.example.nihongobackend.controller.admin;

import jakarta.validation.Valid;
import org.example.nihongobackend.dto.request.admin.AdminUserActiveRequest;
import org.example.nihongobackend.dto.response.admin.AdminUserPageResponse;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.service.admin.AdminUserManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminUserController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public ApiResponse<AdminUserPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(null, adminUserManagementService.listUsers(page, size));
    }

    @PatchMapping("/{id}/active")
    public ApiResponse<Object> setActive(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUserActiveRequest request) {
        adminUserManagementService.setUserActive(id, Boolean.TRUE.equals(request.getActive()));
        return ApiResponse.success("Updated", null);
    }
}
