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
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;
import uk.gov.cshr.civilservant.service.identity.model.BatchProcessResponse;
import uk.gov.cshr.civilservant.utils.CustomAuthentication;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.cshr.civilservant.utils.apiStubs.IdentityServiceStub.*;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Transactional
public class CivilServantControllerIntegrationTest extends BaseIntegrationTest {
    private final Authentication learner = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner");

    @Test
    public void shouldGetCurrentCivilServant() throws Exception {
        mockMvc.perform(
                        get("/civilServants/me")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", equalTo("Learner")))
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(2)))
                .andExpect(jsonPath("$.profession.id", equalTo(1)))
                .andExpect(jsonPath("$.identity.uid", equalTo("learner")));

    }

    @Test
    public void shouldThrowExceptionWhenUpdatingCivilServantToIncorrectOrganisation() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@domain.com", new HashSet<>(Collections.singletonList("LEARNER"))
        ));
        mockMvc.perform(
                        patch("/civilServants/me/organisationalUnit")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(learner))
                                .content("{\"organisationalUnitId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", equalTo("User domain 'domain.com' does not exist on organisation '1', valid domains are: [another-domain.co.uk, cabinetoffice.gov.uk]")))
                .andExpect(jsonPath("$.apiErrorCode.code", equalTo("CS001")))
                .andExpect(jsonPath("$.apiErrorCode.description", equalTo("Civil servant email domain does not match organisation")));

    }

    @Test
    public void shouldUpdateRestrictedCivilServantsOrganisation() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@cabinetoffice.gov.uk", new HashSet<>(Collections.singletonList("LEARNER"))
        ));
        stubPostRemoveReportingAccess(Collections.singletonList("learner"),
                new BatchProcessResponse(Collections.singletonList("learner"), Collections.emptyList()));
        mockMvc.perform(
                        patch("/civilServants/me/organisationalUnit")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(learner))
                                .content("{\"organisationalUnitId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(2)));

    }

    @Test
    public void shouldUpdateUnrestrictedCivilServantsOrganisation() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@cabinetoffice.gov.uk", new HashSet<>(Collections.singletonList("UNRESTRICTED_ORGANISATION"))
        ));
        mockMvc.perform(
                        patch("/civilServants/me/organisationalUnit")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(learner))
                                .content("{\"organisationalUnitId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(2)));
    }
}
