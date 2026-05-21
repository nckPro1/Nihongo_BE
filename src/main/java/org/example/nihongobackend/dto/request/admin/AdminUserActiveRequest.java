package org.example.nihongobackend.dto.request.admin;

import jakarta.validation.constraints.NotNull;

public class AdminUserActiveRequest {
    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
