package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchSkillsMetadataRequest {

    @NotNull
    private Boolean isSynced;

}
