package org.example.nihongobackend.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * PATCH profile: chỉ gửi trường cần đổi (name và/hoặc jlptLevel).
 */
public class UpdateProfileRequest {

    @Size(min = 1, max = 255, message = "Name length must be 1–255")
    private String name;

    @Pattern(regexp = "^N[1-5]$", message = "JLPT level must be N1, N2, N3, N4, or N5")
    private String jlptLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJlptLevel() {
        return jlptLevel;
    }

    public void setJlptLevel(String jlptLevel) {
        this.jlptLevel = jlptLevel;
    }
}
