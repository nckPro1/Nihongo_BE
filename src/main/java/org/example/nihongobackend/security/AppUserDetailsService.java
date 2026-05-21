package org.example.nihongobackend.security;

import org.example.nihongobackend.entity.UserRole;
import org.example.nihongobackend.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        org.example.nihongobackend.entity.User appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserRole role = appUser.getRole() != null ? appUser.getRole() : UserRole.FREE;

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPasswordHash() == null ? "" : appUser.getPasswordHash())
                .roles(role.name())
                .disabled(Boolean.FALSE.equals(appUser.getIsActive()))
                .build();
    }
}
