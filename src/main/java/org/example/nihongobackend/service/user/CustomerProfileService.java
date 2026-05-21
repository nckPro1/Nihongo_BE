package org.example.nihongobackend.service.user;

import org.example.nihongobackend.entity.CustomerProfile;
import org.example.nihongobackend.entity.User;
import org.example.nihongobackend.entity.UserRole;
import org.example.nihongobackend.repository.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hồ sơ khách (học viên): JLPT, Pro. Admin có thể không có dòng; USER luôn có sau {@link #ensureLearnerProfile}.
 */
@Service
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;

    public CustomerProfileService(CustomerProfileRepository customerProfileRepository) {
        this.customerProfileRepository = customerProfileRepository;
    }

    /**
     * USER: trả về bản ghi (tạo mặc định nếu chưa có). ADMIN: chỉ trả về nếu đã có bản ghi, không tạo.
     */
    @Transactional
    public CustomerProfile ensureLearnerProfile(User user) {
        if (user.getRole().isAdmin()) {
            return customerProfileRepository.findById(user.getId()).orElse(null);
        }
        return customerProfileRepository.findById(user.getId()).orElseGet(() -> createDefault(user));
    }

    private CustomerProfile createDefault(User user) {
        CustomerProfile cp = new CustomerProfile();
        cp.setUser(user);
        return customerProfileRepository.save(cp);
    }

    @Transactional
    public CustomerProfile save(CustomerProfile profile) {
        return customerProfileRepository.save(profile);
    }
}
