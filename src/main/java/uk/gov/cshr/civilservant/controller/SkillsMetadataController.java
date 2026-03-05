package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.civilservant.controller.models.FetchSkillsMetadataRequest;
import uk.gov.cshr.civilservant.controller.models.SyncSkillsMetadataRequest;
import uk.gov.cshr.civilservant.controller.v2.models.PageableParams;
import uk.gov.cshr.civilservant.controller.v2.models.SimplePage;
import uk.gov.cshr.civilservant.dto.skills.SkillsMetadataDto;
import uk.gov.cshr.civilservant.service.SkillsMetadataService;

import javax.validation.Valid;

@Slf4j
@RequestMapping("/skills-metadata")
@RestController
public class SkillsMetadataController {

    private final SkillsMetadataService skillsMetadataService;

    public SkillsMetadataController(SkillsMetadataService skillsMetadataService) {
        this.skillsMetadataService = skillsMetadataService;
    }

    @ResponseBody
    @GetMapping
    public SimplePage<SkillsMetadataDto> getSkillsUids(PageableParams pageable, @Valid FetchSkillsMetadataRequest params) {
        return skillsMetadataService.getUids(params, pageable);
    }

    @ResponseBody
    @PostMapping("/sync-uids")
    @ResponseStatus(HttpStatus.OK)
    public void syncUids(@RequestBody SyncSkillsMetadataRequest params) {
        skillsMetadataService.syncUids(params);
    }

}
