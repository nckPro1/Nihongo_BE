package org.example.nihongobackend.service.admin;

import org.example.nihongobackend.dto.request.auth.LoginRequest;
import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.dto.response.auth.LoginResponse;

public interface AdminAuthService {

    LoginResponse login(LoginRequest request);

    AuthUserResponse me();
}
