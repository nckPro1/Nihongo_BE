package org.example.nihongobackend.mapper;

import org.example.nihongobackend.dto.response.auth.AuthUserResponse;
import org.example.nihongobackend.entity.CustomerProfile;
import org.example.nihongobackend.entity.User;

public final class UserResponseMapper {

    private UserResponseMapper() {
    }

    public static AuthUserResponse toAuthUserResponse(User user, CustomerProfile profile) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setAvatar(user.getAvatar());
        String jlpt = profile != null && profile.getJlptLevel() != null && !profile.getJlptLevel().isBlank()
                ? profile.getJlptLevel()
                : "N5";
        response.setJlptLevel(jlpt);
        response.setHasPassword(user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
        response.setRole(user.getRole().name());
        response.setIsPro(profile != null && Boolean.TRUE.equals(profile.getIsPro()));
        return response;
    }
}
