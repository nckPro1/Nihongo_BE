package org.example.nihongobackend.dto.response.auth;

import java.util.UUID;

public class AuthUserResponse {
    private UUID id;
    private String email;
    private String name;
    /** URL ảnh đại diện; có thể null nếu chưa có. */
    private String avatar;
    /** Mức JLPT hiện tại (N5–N1). */
    private String jlptLevel;
    /** Có thể đăng nhập bằng mật khẩu (đã đặt mật khẩu). */
    private Boolean hasPassword;
    private String role;
    private Boolean isPro;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getJlptLevel() {
        return jlptLevel;
    }

    public void setJlptLevel(String jlptLevel) {
        this.jlptLevel = jlptLevel;
    }

    public Boolean getHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(Boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsPro() {
        return isPro;
    }

    public void setIsPro(Boolean pro) {
        isPro = pro;
    }
}
