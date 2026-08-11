package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetCivilServantsForUidsParams {

    private List<String> uids;
    private Long organisationId = null;

}
