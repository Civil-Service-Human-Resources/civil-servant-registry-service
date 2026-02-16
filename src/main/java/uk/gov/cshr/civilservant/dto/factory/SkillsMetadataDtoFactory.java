package uk.gov.cshr.civilservant.dto.factory;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;
import uk.gov.cshr.civilservant.dto.skills.SkillsMetadataSyncDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Service
public class SkillsMetadataDtoFactory {

    public SkillsMetadataSyncDto buildSkillsMetadataSyncDto(Page<CivilServantSkillsMetadata> page) {
        LocalDateTime minLastSynced = null;
        List<String> uids = new ArrayList<>();
        for (CivilServantSkillsMetadata civilServantSkillsMetadata : page.getContent()) {
            String uid = civilServantSkillsMetadata.getCivilServant().getIdentity().getUid();
            if (minLastSynced == null || civilServantSkillsMetadata.getSyncTimestamp().isBefore(minLastSynced)) {
                minLastSynced = civilServantSkillsMetadata.getSyncTimestamp();
            }
            uids.add(uid);
        }
        Integer remaining = toIntExact(page.getTotalElements()) - uids.size();
        return new SkillsMetadataSyncDto(uids, minLastSynced, remaining);
    }

}
