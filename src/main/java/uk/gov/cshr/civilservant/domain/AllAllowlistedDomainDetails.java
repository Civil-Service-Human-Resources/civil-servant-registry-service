package uk.gov.cshr.civilservant.domain;

import org.springframework.data.rest.core.config.Projection;

@Projection(
        name = "allAllowlistedDomainDetails",
        types = {AllowlistedDomain.class}
)
public interface AllAllowlistedDomainDetails {
    String getDomain();
}
