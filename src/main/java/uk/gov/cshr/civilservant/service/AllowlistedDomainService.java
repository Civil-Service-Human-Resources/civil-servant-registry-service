package uk.gov.cshr.civilservant.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.domain.AllowlistedDomain;
import uk.gov.cshr.civilservant.repository.AllowlistedDomainRepository;

import java.util.List;

@Service
public class AllowlistedDomainService {
    private final AllowlistedDomainRepository allowlistedDomainRepository;

    public AllowlistedDomainService(AllowlistedDomainRepository allowlistedDomainRepository) {
        this.allowlistedDomainRepository = allowlistedDomainRepository;
    }

    public List<AllowlistedDomain> getAllAllowlistedDomains(){
        return allowlistedDomainRepository.findAll();
    }
}
