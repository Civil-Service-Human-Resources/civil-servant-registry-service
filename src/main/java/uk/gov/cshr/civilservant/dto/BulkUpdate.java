package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkUpdate {

    private List<Long> updatedIds;
    private List<Long> skippedIds;
}
