package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.civilservant.controller.models.SyncSkillsMetadataRequest;
import uk.gov.cshr.civilservant.dto.skills.SkillsMetadataSyncDto;
import uk.gov.cshr.civilservant.service.SkillsMetadataService;

@Slf4j
@RequestMapping("/skills-metadata")
@RestController
public class SkillsMetadataController {

    private final SkillsMetadataService skillsMetadataService;

    public SkillsMetadataController(SkillsMetadataService skillsMetadataService) {
        this.skillsMetadataService = skillsMetadataService;
    }

    @ResponseBody
    @PostMapping("/sync-uids")
    public SkillsMetadataSyncDto syncSkillsUids(@RequestBody SyncSkillsMetadataRequest params) {
        return skillsMetadataService.syncUsers(params);
    }

}
