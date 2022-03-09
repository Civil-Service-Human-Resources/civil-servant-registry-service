package uk.gov.cshr.civilservant.dto;

import lombok.Data;

@Data
public class AllowlistedDomainDto {
    private String domain;

    public AllowlistedDomainDto(String domain) {
        this.domain = domain;
    }

    public AllowlistedDomainDto() {
    }
}
