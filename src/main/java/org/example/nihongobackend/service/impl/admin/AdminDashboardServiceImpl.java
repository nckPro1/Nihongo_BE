package org.example.nihongobackend.service.impl.admin;

import org.example.nihongobackend.dto.response.admin.AdminDashboardStatsResponse;
import org.example.nihongobackend.repository.UserRepository;
import org.example.nihongobackend.service.admin.AdminDashboardService;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;

    public AdminDashboardServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AdminDashboardStatsResponse stats() {
        AdminDashboardStatsResponse r = new AdminDashboardStatsResponse();
        r.setTotalUsers(userRepository.count());
        r.setActiveUsers(userRepository.countByIsActiveTrue());
        return r;
    }
}
