package org.example.nihongobackend.dto.response.auth;

import java.util.UUID;

public class AuthUserResponse {
    private UUID id;
    private String email;
    private String name;
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
