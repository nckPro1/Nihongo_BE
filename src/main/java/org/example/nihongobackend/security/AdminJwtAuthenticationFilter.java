package org.example.nihongobackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;

@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_API_PREFIX = "/api/admin";
    private static final String BLOG_ADMIN_PREFIX = "/api/blog/admin";
    private static final String MEDIA_PREFIX = "/api/media";
    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    private final AdminJwtService adminJwtService;
    private final AppUserDetailsService userDetailsService;

    public AdminJwtAuthenticationFilter(AdminJwtService adminJwtService, AppUserDetailsService userDetailsService) {
        this.adminJwtService = adminJwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = PATH_HELPER.getPathWithinApplication(request);
        // Only filter admin paths: /api/admin/**, /api/blog/admin/**, /api/media/**
        if (!path.startsWith(ADMIN_API_PREFIX) &&
            !path.startsWith(BLOG_ADMIN_PREFIX) &&
            !path.startsWith(MEDIA_PREFIX)) {
            return true;
        }
        // Skip filter for login endpoint
        if ("/api/admin/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!adminJwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = adminJwtService.extractEmail(token);
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (email != null && (existing == null || existing instanceof AnonymousAuthenticationToken)) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                boolean isAdmin = userDetails.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                if (!isAdmin) {
                    filterChain.doFilter(request, response);
                    return;
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (UsernameNotFoundException ignored) {
                // no-op
            }
        }

        filterChain.doFilter(request, response);
    }
}
