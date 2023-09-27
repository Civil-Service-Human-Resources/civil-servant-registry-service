package uk.gov.cshr.civilservant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.service.DomainService;

import java.util.List;

@RestController()
@RequestMapping("/domains")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public ResponseEntity<List<Domain>> getDomains() {
        return ResponseEntity.ok(domainService.getDomains());
    }
}
