package uk.gov.cshr.civilservant.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.UriToEntityConverter;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class EntityUriResolver {
    private final UriToEntityConverter uriToEntityConverter;

    public EntityUriResolver(
            PersistentEntities persistentEntities,
            RepositoryInvokerFactory repositoryInvokerFactory,
            @Qualifier("defaultConversionService") ConversionService conversionService) {

        this.uriToEntityConverter = new UriToEntityConverter(
                persistentEntities,
                repositoryInvokerFactory,
                () -> conversionService
        );
    }

    public <T> T resolve(String uri, Class<T> entityType) {
        return entityType.cast(
                uriToEntityConverter.convert(
                        URI.create(uri),
                        TypeDescriptor.valueOf(URI.class),
                        TypeDescriptor.valueOf(entityType)
                )
        );
    }
}
