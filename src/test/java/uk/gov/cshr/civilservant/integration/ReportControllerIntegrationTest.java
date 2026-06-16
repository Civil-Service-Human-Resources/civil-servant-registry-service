package uk.gov.cshr.civilservant.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.config.IntegrationTestUserConfig;
import uk.gov.cshr.civilservant.utils.CustomAuthentication;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Transactional
public class ReportControllerIntegrationTest extends BaseIntegrationTest {
    private final Authentication learner = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner");

    @Test
    public void shouldGetACivilServantWithUid() throws Exception {
        mockMvc.perform(
                        get("/report/civil-servants-for-uids")
                                .param("uids", "skills-4")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills-4.id", equalTo("1103")))
                .andExpect(jsonPath("$.skills-4.uid", equalTo("skills-4")))
                .andExpect(jsonPath("$.skills-4.name", equalTo("Skills 4")))
                .andExpect(jsonPath("$.skills-4.email", equalTo(null)))
                .andExpect(jsonPath("$.skills-4.organisationCode", equalTo("D21")))
                .andExpect(jsonPath("$.skills-4.organisationId", equalTo(4)))
                .andExpect(jsonPath("$.skills-4.organisation", equalTo("Department of Health & Social Care | DH Core")))
                .andExpect(jsonPath("$.skills-4.profession", equalTo("Analysis")))
                .andExpect(jsonPath("$.skills-4.otherAreasOfWork", equalTo(null)))
                .andExpect(jsonPath("$.skills-4.grade", equalTo("Administrative assistant")))
                .andExpect(jsonPath("$.skills-4.lineManagerUid", equalTo(null)));

    }

    @Test
    public void shouldGetACivilServantWithUidAndOrganisationId() throws Exception {
        mockMvc.perform(
                        get("/report/civil-servants-for-uids")
                                .param("uids", "skills-4,skills-3")
                                .param("organisationalUnitId", "2")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(jsonPath("$.skills-4.id", equalTo("1103")))
                .andExpect(jsonPath("$.skills-4.uid", equalTo("skills-4")))
                .andExpect(jsonPath("$.skills-4.name", equalTo("Skills 4")))
                .andExpect(jsonPath("$.skills-4.email", equalTo(null)))
                .andExpect(jsonPath("$.skills-4.organisationCode", equalTo("D21")))
                .andExpect(jsonPath("$.skills-4.organisationId", equalTo(4)))
                .andExpect(jsonPath("$.skills-4.organisation", equalTo("Department of Health & Social Care | DH Core")))
                .andExpect(jsonPath("$.skills-4.profession", equalTo("Analysis")))
                .andExpect(jsonPath("$.skills-4.otherAreasOfWork", equalTo(null)))
                .andExpect(jsonPath("$.skills-4.grade", equalTo("Administrative assistant")))
                .andExpect(jsonPath("$.skills-4.lineManagerUid", equalTo(null)))
                .andExpect(jsonPath("$.skills-3.id", equalTo("1102")))
                .andExpect(jsonPath("$.skills-3.uid", equalTo("skills-3")))
                .andExpect(jsonPath("$.skills-3.name", equalTo("Skills 3")))
                .andExpect(jsonPath("$.skills-3.email", equalTo(null)))
                .andExpect(jsonPath("$.skills-3.organisationCode", equalTo("dh")))
                .andExpect(jsonPath("$.skills-3.organisationId", equalTo(2)))
                .andExpect(jsonPath("$.skills-3.organisation", equalTo("Department of Health & Social Care")))
                .andExpect(jsonPath("$.skills-3.profession", equalTo("Analysis")))
                .andExpect(jsonPath("$.skills-3.otherAreasOfWork", equalTo(null)))
                .andExpect(jsonPath("$.skills-3.grade", equalTo("Administrative assistant")))
                .andExpect(jsonPath("$.skills-3.lineManagerUid", equalTo(null)));

    }


}
