package com.app; // ← Make sure this is correct

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.app.repositories") // ← THIS is the fix
public class CosmeticsAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(CosmeticsAppApplication.class, args);
    }
}
