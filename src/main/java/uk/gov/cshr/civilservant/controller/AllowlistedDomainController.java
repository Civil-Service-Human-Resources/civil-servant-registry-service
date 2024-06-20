package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.civilservant.domain.AllowlistedDomain;
import uk.gov.cshr.civilservant.dto.AllowlistedDomainDto;
import uk.gov.cshr.civilservant.dto.factory.AllowlistedDomainDtoFactory;
import uk.gov.cshr.civilservant.service.AllowlistedDomainService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/domains")
public class AllowlistedDomainController {
    private AllowlistedDomainService service;
    private AllowlistedDomainDtoFactory dtoFactory;

    public AllowlistedDomainController(AllowlistedDomainService service, AllowlistedDomainDtoFactory dtoFactory) {
        this.service = service;
        this.dtoFactory = dtoFactory;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AllowlistedDomainDto>> getAllDomains(){
        List<AllowlistedDomain> allowlistedDomains = service.getAllAllowlistedDomains();
        List<AllowlistedDomainDto> dtos = allowlistedDomains.stream()
                .map(domain -> dtoFactory.create(domain))
                .collect(Collectors.toList());

        return ResponseEntity.ok((dtos));
    }
}
