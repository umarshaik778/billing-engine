package com.example.billing.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestConfiguration
public class TestConfig {

    @Bean
    public Void cleanTestDirectories() throws Exception {

        Path base = Paths.get("data");

        if (Files.exists(base)) {
            Files.walk(base)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {
                        }
                    });
        }
        return null;
    }
}
