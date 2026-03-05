package uk.gov.cshr.civilservant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.controller.models.FetchSkillsMetadataRequest;
import uk.gov.cshr.civilservant.controller.models.SyncSkillsMetadataRequest;
import uk.gov.cshr.civilservant.controller.v2.models.PageableParams;
import uk.gov.cshr.civilservant.controller.v2.models.SimplePage;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;
import uk.gov.cshr.civilservant.dto.skills.SkillsMetadataDto;
import uk.gov.cshr.civilservant.repository.SkillsMetadataRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SkillsMetadataService {

    private final SkillsMetadataRepository skillsMetadataRepository;
    private final Clock clock;

    public SkillsMetadataService(SkillsMetadataRepository skillsMetadataRepository, Clock clock) {
        this.skillsMetadataRepository = skillsMetadataRepository;
        this.clock = clock;
    }

    public SimplePage<SkillsMetadataDto> getUids(FetchSkillsMetadataRequest params, PageableParams pageableParams) {
        Pageable pageable = pageableParams.getAsPageable();
        Page<CivilServantSkillsMetadata> skillsMetadataPage = params.getIsSynced() ? skillsMetadataRepository.getBySynced(pageable, params.getSyncTimestampLte()) : skillsMetadataRepository.getByNotSynced(pageable);
        List<SkillsMetadataDto> dtos = skillsMetadataPage.getContent().stream()
                .map(c -> new SkillsMetadataDto(c.getCivilServant().getIdentity().getUid(), c.getSyncTimestamp())).collect(Collectors.toList());
        return new SimplePage<>(dtos, skillsMetadataPage.getTotalElements(), pageable);
    }

    public void syncUids(SyncSkillsMetadataRequest params) {
        List<CivilServantSkillsMetadata> metadata = skillsMetadataRepository.getAllByCivilServant_Identity_UidIn(params.getUids());
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("Syncing {} accounts", metadata.size());
        metadata.forEach(data -> data.setSyncTimestamp(now));
        skillsMetadataRepository.saveAll(metadata);
    }
}
