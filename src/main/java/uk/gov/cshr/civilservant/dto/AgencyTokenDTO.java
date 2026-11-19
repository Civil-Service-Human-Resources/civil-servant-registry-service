package uk.gov.cshr.civilservant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.civilservant.validation.ValidCapacity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@Data
public class AgencyTokenDTO {

    @NotEmpty(message = "token cannot be empty")
    @NotBlank(message = "token cannot be blank")
    @NotNull(message = "token cannot be null")
    private String token;

    @ValidCapacity
    private int capacity;

    private String uid;

    @NotEmpty
    private Set<AgencyDomainDTO> agencyDomains;
}
