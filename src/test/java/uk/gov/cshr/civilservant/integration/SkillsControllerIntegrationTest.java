package uk.gov.cshr.civilservant.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.config.IntegrationTestUserConfig;
import uk.gov.cshr.civilservant.config.MockClockConfig;
import uk.gov.cshr.civilservant.controller.v2.models.PageableParams;
import uk.gov.cshr.civilservant.domain.CivilServantSkillsMetadata;
import uk.gov.cshr.civilservant.repository.SkillsMetadataRepository;
import uk.gov.cshr.civilservant.utils.CustomAuthentication;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Transactional
@Import(MockClockConfig.class)
public class SkillsControllerIntegrationTest extends BaseIntegrationTest {

    private final Authentication auth = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner");

    @Autowired
    private SkillsMetadataRepository skillsMetadataRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Test
    @Transactional
    public void shouldSyncExistingUids() throws Exception {
        mockMvc.perform(
                        post("/skills-metadata/sync-uids")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"userCount\":  2, \"isSynced\": true}")
                                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uids.length()", equalTo(2)))
                .andExpect(jsonPath("$.uids[0]", equalTo("skills-3")))
                .andExpect(jsonPath("$.uids[1]", equalTo("skills-4")))
                .andExpect(jsonPath("$.minSyncTimestamp", equalTo("2022-01-01 10:00:00")))
                .andExpect(jsonPath("$.remainingUserCount", equalTo(2)));
        Map<String, CivilServantSkillsMetadata> map = new HashMap<>();
        skillsMetadataRepository.getAll(new PageableParams(0, 6).getAsPageable()).getContent()
                .forEach(s -> map.put(s.getCivilServant().getIdentity().getUid(), s));
        assertNull(map.get("skills-1").getSyncTimestamp());
        assertNull(map.get("skills-2").getSyncTimestamp());
        assertEquals("2023-01-01T10:00:00", map.get("skills-3").getSyncTimestamp().format(formatter));
        assertEquals("2023-01-01T10:00:00", map.get("skills-4").getSyncTimestamp().format(formatter));
        assertEquals("2022-03-01T10:00:00", map.get("skills-5").getSyncTimestamp().format(formatter));
        assertEquals("2022-04-01T10:00:00", map.get("skills-6").getSyncTimestamp().format(formatter));
    }

    @Test
    @Transactional
    public void shouldSyncNewUids() throws Exception {
        mockMvc.perform(
                        post("/skills-metadata/sync-uids")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"userCount\":  2, \"isSynced\": false}")
                                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uids.length()", equalTo(2)))
                .andExpect(jsonPath("$.uids[0]", equalTo("skills-1")))
                .andExpect(jsonPath("$.uids[1]", equalTo("skills-2")))
                .andExpect(jsonPath("$.minSyncTimestamp", equalTo(null)))
                .andExpect(jsonPath("$.remainingUserCount", equalTo(0)));
        Map<String, CivilServantSkillsMetadata> map = new HashMap<>();
        skillsMetadataRepository.getAll(new PageableParams(0, 6).getAsPageable()).getContent()
                .forEach(s -> map.put(s.getCivilServant().getIdentity().getUid(), s));
        assertEquals("2023-01-01T10:00:00", map.get("skills-1").getSyncTimestamp().format(formatter));
        assertEquals("2023-01-01T10:00:00", map.get("skills-2").getSyncTimestamp().format(formatter));
        assertEquals("2022-01-01T10:00:00", map.get("skills-3").getSyncTimestamp().format(formatter));
        assertEquals("2022-02-01T10:00:00", map.get("skills-4").getSyncTimestamp().format(formatter));
        assertEquals("2022-03-01T10:00:00", map.get("skills-5").getSyncTimestamp().format(formatter));
        assertEquals("2022-04-01T10:00:00", map.get("skills-6").getSyncTimestamp().format(formatter));
    }

}
