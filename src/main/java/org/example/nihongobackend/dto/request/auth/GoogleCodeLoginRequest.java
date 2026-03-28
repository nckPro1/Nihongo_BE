package org.example.nihongobackend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public class GoogleCodeLoginRequest {
    @NotBlank(message = "Google authorization code is required")
    private String code;

    @NotBlank(message = "Google redirect URI is required")
    private String redirectUri;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
