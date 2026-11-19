package uk.gov.cshr.civilservant.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.changeDefaultPropertyInclusion(inclusion ->
                    inclusion.withValueInclusion(JsonInclude.Include.ALWAYS));
            builder.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        };
    }
}