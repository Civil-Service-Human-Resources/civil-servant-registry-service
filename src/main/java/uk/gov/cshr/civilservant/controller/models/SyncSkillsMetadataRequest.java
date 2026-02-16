package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncSkillsMetadataRequest {

    @NotNull
    @Min(1)
    @Max(200)
    private Integer userCount;
    @NotNull
    private Boolean isSynced;

}
