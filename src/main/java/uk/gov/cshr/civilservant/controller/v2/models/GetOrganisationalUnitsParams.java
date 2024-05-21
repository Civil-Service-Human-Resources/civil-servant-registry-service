package uk.gov.cshr.civilservant.controller.v2.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOrganisationalUnitsParams {

    List<Long> ids = Collections.emptyList();
    boolean fetchChildren = false;
    boolean formatName = false;
}
