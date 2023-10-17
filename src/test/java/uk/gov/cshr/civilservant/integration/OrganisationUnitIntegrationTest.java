package uk.gov.cshr.civilservant.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.civilservant.controller.CSRSControllerTestBase;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class OrganisationUnitIntegrationTest extends CSRSControllerTestBase {

    @Test
    public void shouldGetOrganisationalUnitWithDomains() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/v2/organisationalUnits/1")
                                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", equalTo("Cabinet Office")))
                .andExpect(jsonPath("$.code", equalTo("co")))
                .andExpect(jsonPath("$.domains[0].domain", equalTo("another-domain.co.uk")))
                .andExpect(jsonPath("$.domains[1].domain", equalTo("cabinetoffice.gov.uk")));

    }

    @Test
    public void shouldGetMultipleOrganisationalUnits() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/v2/organisationalUnits")
                                .accept(APPLICATION_JSON)
                                .param("ids", "2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", equalTo(2)))
                .andExpect(jsonPath("$.content[1].id", equalTo(3)));

    }

    @Test
    public void shouldGetMultipleOrganisationalUnitsWithPagination() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/v2/organisationalUnits?page=1&size=5")
                                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", equalTo(5)))
                .andExpect(jsonPath("$.page", equalTo(1)))
                .andExpect(jsonPath("$.size", equalTo(5)));

    }

    @Test
    public void shouldAddNewDomain() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/1/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domain.domain", equalTo("test.org")))
                .andExpect(jsonPath("$.primaryOrganisationId", equalTo(1)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds", empty()))
                .andExpect(jsonPath("$.skippedChildOrganisationIds", empty()));

    }

    @Test
    public void shouldAddNewDomainAndCascade() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/31/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test-two.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domain.domain", equalTo("test-two.org")))
                .andExpect(jsonPath("$.primaryOrganisationId", equalTo(31)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds.length()", equalTo(2)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds[0]", equalTo(32)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds[1]", equalTo(33)))
                .andExpect(jsonPath("$.skippedChildOrganisationIds", empty()));

    }

    @Test
    public void shouldAddNewDomainAndNotCascade() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/33/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test-three.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domain.domain", equalTo("test-three.org")))
                .andExpect(jsonPath("$.primaryOrganisationId", equalTo(33)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds", empty()))
                .andExpect(jsonPath("$.skippedChildOrganisationIds", empty()));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/32/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test-three.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domain.domain", equalTo("test-three.org")))
                .andExpect(jsonPath("$.primaryOrganisationId", equalTo(32)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds", empty()))
                .andExpect(jsonPath("$.skippedChildOrganisationIds.length()", equalTo(1)))
                .andExpect(jsonPath("$.skippedChildOrganisationIds[0]", equalTo(33)));
    }

    @Test
    public void shouldNotAddDomainIfOrgDoesNotExist() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/900/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test.org\"}"))
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldNotAddDomainIfInvalidFormat() throws Exception {

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/33/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"incorrectFormat\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apiErrorCode.code", equalTo("OU002")))
                .andExpect(jsonPath("$.apiErrorCode.description", equalTo("Invalid domain format. Correct format is: 'example.gov.uk'")));

    }

    @Test
    public void shouldNotAddDomainIfOrgAlreadyHasDomain() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/33/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test-four.org\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.domain.domain", equalTo("test-four.org")))
                .andExpect(jsonPath("$.primaryOrganisationId", equalTo(33)))
                .andExpect(jsonPath("$.updatedChildOrganisationIds", empty()))
                .andExpect(jsonPath("$.skippedChildOrganisationIds", empty()));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/organisationalUnits/33/domains")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"domain\": \"test-four.org\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apiErrorCode.code", equalTo("OU001")))
                .andExpect(jsonPath("$.apiErrorCode.description", equalTo("Domain already exists on the organisational unit")));

    }
}
