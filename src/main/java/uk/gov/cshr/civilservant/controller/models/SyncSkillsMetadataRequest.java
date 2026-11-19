package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncSkillsMetadataRequest {

    @NotNull
    @NotEmpty
    private List<String> uids;

}
