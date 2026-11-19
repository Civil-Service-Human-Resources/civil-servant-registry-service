package uk.gov.cshr.civilservant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI api(){
        return new OpenAPI()
                .info(new Info()
                        .title("Civil Servant Registry Service API")
                        .description("The API used for retrieving and updating data in the Civil Servant Registry for Civil Service Learning")
                        .version("v1"));
    }
}
