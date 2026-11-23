package uk.gov.cshr.civilservant;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

public class PropertySourceLogger
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();

        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> enumerable) {
                for (String name : enumerable.getPropertyNames()) {
                    if (name.toLowerCase().contains("spring.jackson")) {
                        System.err.println(
                                source.getName() + " : " + name
                                        + " = " + source.getProperty(name)
                        );
                    }
                }
            }

            Object value = source.getProperty("spring.jackson.serialization");

            if (value != null) {
                System.err.println(
                        "SERIALIZATION MAP: " + source.getName()
                                + " = " + value
                );
            }
        }
    }
}