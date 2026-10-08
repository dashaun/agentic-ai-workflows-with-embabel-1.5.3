package com.embabel.demo;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmbabelShellDemoApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(EmbabelShellDemoApplication.class);
        // The TypeSafe starter fails on a blank key, so only pass TYPESAFE_API_KEY on when it is set.
        String typeSafeKey = System.getenv("TYPESAFE_API_KEY");
        if (typeSafeKey != null && !typeSafeKey.isBlank()) {
            application.setDefaultProperties(Map.of("spring.ai.typesafe.api-key", typeSafeKey));
        }
        application.run(args);
    }
}
