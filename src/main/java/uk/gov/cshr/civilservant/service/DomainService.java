package uk.gov.cshr.civilservant.service;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.repository.DomainRepository;

import java.util.List;

@Service
public class DomainService {

    private final DomainRepository repository;

    public DomainService(DomainRepository repository) {
        this.repository = repository;
    }

    public List<Domain> getDomains() {
        return Lists.newArrayList(repository.findAll());
    }
}
