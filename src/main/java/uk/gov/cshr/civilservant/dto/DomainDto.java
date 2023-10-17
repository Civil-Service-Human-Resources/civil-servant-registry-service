package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.validation.domain.ValidDomain;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainDto {
    Long id;

    @ValidDomain
    String domain;

    LocalDateTime createdTimestamp;

    public DomainDto(Domain domain) {
        this(
                domain.getId(),
                domain.getDomain(),
                domain.getCreatedTimestamp()
        );
    }
}
