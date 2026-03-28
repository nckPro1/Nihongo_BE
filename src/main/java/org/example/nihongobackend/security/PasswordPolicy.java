package org.example.nihongobackend.security;

import org.example.nihongobackend.exception.BadRequestException;

public final class PasswordPolicy {

    public static final String MESSAGE =
            "Password must be at least 8 characters and include at least 1 uppercase letter and 1 number";

    private PasswordPolicy() {
    }

    public static void validateOrThrow(String password) {
        if (password == null
                || password.length() < 8
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new BadRequestException(MESSAGE);
        }
    }
}
