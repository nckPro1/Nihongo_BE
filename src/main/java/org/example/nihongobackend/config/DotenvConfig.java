package org.example.nihongobackend.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class DotenvConfig {
    private DotenvConfig() {
    }

    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
