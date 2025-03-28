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
import static uk.gov.cshr.civilservant.utils.apiStubs.IdentityServiceStub.stubGetIdentityWithUid;
import static uk.gov.cshr.civilservant.utils.apiStubs.IdentityServiceStub.stubPostClientToken;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Transactional
public class CivilServantControllerIntegrationTest extends BaseIntegrationTest {
    private final Authentication learner = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner");
    private final Authentication civilServant1010 = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner-with-other-orgs");

    @Test
    public void shouldACivilServantWithUid() throws Exception {
        mockMvc.perform(
                        get("/civilServants/resource/learner")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", equalTo("Learner")))
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(2)))
                .andExpect(jsonPath("$.grade.id", equalTo(1)))
                .andExpect(jsonPath("$.profession.id", equalTo(1)))
                .andExpect(jsonPath("$.identity.uid", equalTo("learner")));

    }

    @Test
    public void shouldGetCurrentCivilServant() throws Exception {
        mockMvc.perform(
                        get("/civilServants/me")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", equalTo("Learner")))
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(2)))
                .andExpect(jsonPath("$.grade.id", equalTo(1)))
                .andExpect(jsonPath("$.profession.id", equalTo(1)))
                .andExpect(jsonPath("$.otherOrganisationalUnits.length()", equalTo(1)))
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
                .andExpect(jsonPath("$.errors[0]", equalTo("User domain 'domain.com' does not exist on organisation '1' or any associated agency tokens")))
                .andExpect(jsonPath("$.apiErrorCode.code", equalTo("CS001")))
                .andExpect(jsonPath("$.apiErrorCode.description", equalTo("Civil servant email domain does not match organisation")));

    }

    @Test
    public void shouldReturnFullCivilServantProfile() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@domain.com", new HashSet<>(Collections.singletonList("LEARNER"))
        ));
        stubGetIdentityWithUid("manager", new IdentityDTO(
                "manager", "manager@domain.com", new HashSet<>(Collections.singletonList("LEARNER"))
        ));
        mockMvc.perform(
                        get("/civilServants/resource/learner/profile")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.lineManagerName", equalTo("Manager")))
                .andExpect(jsonPath("$.lineManagerEmail", equalTo("manager@domain.com")));

    }

    @Test
    public void shouldUpdateRestrictedCivilServantsOrganisation() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@cabinetoffice.gov.uk", new HashSet<>(Collections.singletonList("LEARNER"))
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

    @Test
    public void shouldUpdateRestrictedCivilServantsOrganisationCaseInsensitive() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@CABINETOFFICE.gov.uk", new HashSet<>(Collections.singletonList("LEARNER"))
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

    @Test
    public void shouldUpdateRestrictedCivilServantsAgencyOrganisation() throws Exception {
        stubPostClientToken();
        stubGetIdentityWithUid("learner", new IdentityDTO(
                "learner", "learner@mydomain.com", new HashSet<>(Collections.singletonList("LEARNER"))
        ));
        mockMvc.perform(
                        patch("/civilServants/me/organisationalUnit")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(learner))
                                .content("{\"organisationalUnitId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationalUnit.id", equalTo(1)));

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

    @Test
    public void testCivilServantsOtherOrganisationalUnitsGetEndpointReturnsArrayOfTwoOrganisationalUnits() throws Exception {
        mockMvc.perform(
                get("/civilServants/1010/otherOrganisationalUnits")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .with(authentication(civilServant1010)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.organisationalUnits.length()", equalTo(2)));
    }

    @Test
    public void testCivilServantsPatchEndpointPerformsOrgUnitPatch() throws Exception {
        mockMvc.perform(
                        patch("/civilServants/1010")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .with(authentication(civilServant1010))
                                .content("{\"otherOrganisationalUnits\": [\"/organisationalUnits/15\", \"/organisationalUnits/16\"]}"))
                .andExpect(status().isOk());
    }
}
