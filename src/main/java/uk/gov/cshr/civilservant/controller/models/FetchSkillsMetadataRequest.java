package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchSkillsMetadataRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime syncTimestampLte;

    private boolean synced;

    public boolean getIsSynced() {
        return synced || syncTimestampLte != null;
    }

}
