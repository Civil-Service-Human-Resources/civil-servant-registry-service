package uk.gov.cshr.civilservant.dto.factory;

import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.AllowlistedDomain;
import uk.gov.cshr.civilservant.dto.AllowlistedDomainDto;

@Component
public class AllowlistedDomainDtoFactory extends DtoFactory<AllowlistedDomainDto, AllowlistedDomain>{
    @Override
    public AllowlistedDomainDto create(AllowlistedDomain allowlistedDomain) {
        AllowlistedDomainDto allowlistedDomainDto = new AllowlistedDomainDto();
        allowlistedDomainDto.setDomain(allowlistedDomain.getDomain());
        return allowlistedDomainDto;
    }
}
