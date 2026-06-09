package uk.gov.cshr.civilservant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class MockClockConfig {

    @Bean
    @Primary
    public Clock getMockClock() {
        return Clock.fixed(Instant.parse("2023-01-01T10:00:00.000Z"), ZoneId.of("UTC"));
    }
}
