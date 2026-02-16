package uk.gov.cshr.civilservant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.controller.models.SyncSkillsMetadataRequest;
import uk.gov.cshr.civilservant.controller.v2.models.PageableParams;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;
import uk.gov.cshr.civilservant.dto.factory.SkillsMetadataDtoFactory;
import uk.gov.cshr.civilservant.dto.skills.SkillsMetadataSyncDto;
import uk.gov.cshr.civilservant.repository.SkillsMetadataRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SkillsMetadataService {

    private final SkillsMetadataRepository skillsMetadataRepository;
    private final SkillsMetadataDtoFactory skillsMetadataDtoFactory;
    private final Clock clock;

    public SkillsMetadataService(SkillsMetadataRepository skillsMetadataRepository, SkillsMetadataDtoFactory skillsMetadataDtoFactory, Clock clock) {
        this.skillsMetadataRepository = skillsMetadataRepository;
        this.skillsMetadataDtoFactory = skillsMetadataDtoFactory;
        this.clock = clock;
    }

    public SkillsMetadataSyncDto syncUsers(SyncSkillsMetadataRequest params) {
        Pageable pageable = new PageableParams(0, params.getUserCount()).getAsPageable();
        Page<CivilServantSkillsMetadata> skillsMetadataPage = params.getIsSynced() ? skillsMetadataRepository.getBySynced(pageable) : skillsMetadataRepository.getByNotSynced(pageable);
        SkillsMetadataSyncDto skillsMetadataSyncDto = skillsMetadataDtoFactory.buildSkillsMetadataSyncDto(skillsMetadataPage);
        log.info("Setting {} civil servants to synced", skillsMetadataPage.getSize());
        LocalDateTime now = LocalDateTime.now(clock);
        List<CivilServantSkillsMetadata> updatedMetadata = skillsMetadataPage.getContent().stream().peek(
                skills -> skills.setSyncTimestamp(now)).collect(Collectors.toList());
        skillsMetadataRepository.saveAll(updatedMetadata);
        return skillsMetadataSyncDto;
    }
}
