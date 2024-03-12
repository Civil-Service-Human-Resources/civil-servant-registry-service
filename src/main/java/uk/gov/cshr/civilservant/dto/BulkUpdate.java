package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.civilservant.domain.RegistryEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUpdate<T extends RegistryEntity> {

    private List<T> updated = new ArrayList<>();
    private List<T> skipped = new ArrayList<>();

    public List<Long> getUpdatedIds() {
        return this.getUpdated().stream().map(RegistryEntity::getId).collect(Collectors.toList());
    }

    public List<Long> getSkippedIds() {
        return this.getSkipped().stream().map(RegistryEntity::getId).collect(Collectors.toList());
    }
}
