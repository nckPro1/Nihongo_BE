package org.example.nihongobackend;

import org.example.nihongobackend.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NihongoBackendApplication {

    public static void main(String[] args) {
        DotenvConfig.load();
        SpringApplication.run(NihongoBackendApplication.class, args);
    }

}
