package org.example.nihongobackend.controller.admin;

import org.example.nihongobackend.dto.response.admin.AdminDashboardStatsResponse;
import org.example.nihongobackend.dto.response.common.ApiResponse;
import org.example.nihongobackend.service.admin.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/stats")
    public ApiResponse<AdminDashboardStatsResponse> stats() {
        return ApiResponse.success(null, adminDashboardService.stats());
    }
}
