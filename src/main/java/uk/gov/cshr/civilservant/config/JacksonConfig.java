package uk.gov.cshr.civilservant.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.serializationInclusion(JsonInclude.Include.ALWAYS);
            builder.featuresToEnable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}