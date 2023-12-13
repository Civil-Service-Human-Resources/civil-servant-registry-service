package uk.gov.cshr.civilservant.service.identity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessResponse {
    private List<String> successfulIds = Collections.emptyList();
    private List<String> failedIds = Collections.emptyList();
}
