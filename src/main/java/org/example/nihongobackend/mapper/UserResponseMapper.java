package org.example.nihongobackend.mapper;

import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.entity.User;

public final class UserResponseMapper {

    private UserResponseMapper() {
    }

    public static AuthUserResponse toAuthUserResponse(User user) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setAvatar(user.getAvatar());
        response.setJlptLevel(user.getJlptLevel());
        response.setHasPassword(user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
        response.setRole(user.getRole());
        response.setIsPro(Boolean.TRUE.equals(user.getIsPro()));
        return response;
    }
}
