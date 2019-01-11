package uk.gov.cshr.civilservant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

/**
 * Main Spring application configuration and entry point.
 */
@SpringBootApplication
@EnableSpringConfigured
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
