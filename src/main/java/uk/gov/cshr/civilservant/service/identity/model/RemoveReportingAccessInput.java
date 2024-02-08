package uk.gov.cshr.civilservant.service.identity.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RemoveReportingAccessInput {
    private List<String> uids;
}
