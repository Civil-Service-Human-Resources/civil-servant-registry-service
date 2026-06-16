package uk.gov.cshr.civilservant.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyTokenDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.service.CivilServantService;
import uk.gov.cshr.civilservant.service.OrganisationalUnitService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;
import uk.gov.cshr.civilservant.utils.JsonUtils;
import uk.gov.cshr.civilservant.utils.OrganisationalUnitTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class OrganisationalUnitControllerTest extends CSRSControllerTestBase {

    private static final String WL_DOMAIN = "mydomain.com";
    private static final String NHS_GLASGOW_DOMAIN = "nhsglasgow.gov.uk";
    private static final String UID = "myuid";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @MockBean
    private OrganisationalUnitService organisationalUnitService;

    @MockBean
    private CivilServantService civilServantService;

    private String requestBodyAgencyTokenAsAString;

    private AgencyTokenDTO dto;

    private List<OrganisationalUnit> completeList;

    private List<OrganisationalUnit> filteredList;

    @Before
    public void setUp() {
        dto = AgencyTokenTestingUtils.createAgencyTokenDTO();
        completeList = new ArrayList<>(10);
        for(int i=0; i<10; i++) {
            completeList.add(OrganisationalUnitTestUtils.buildOrgUnit("wl", i, "whitelisted-domain"));
        }
        filteredList = new ArrayList<>(3);
        for(int i=0; i<3; i++) {
            filteredList.add(OrganisationalUnitTestUtils.buildOrgUnit("f", i, "agency-domain"));
        }
        when(civilServantService.getCivilServantUid()).thenReturn(UID);
    }

    @Test
    public void shouldReturnOkIfRequestingGetToken() throws Exception {
        AgencyTokenResponseDto responseDto = AgencyTokenTestingUtils.getAgencyTokenResponseDto();
        String expectedDomainName =  responseDto.getAgencyDomains().stream().findFirst().get().getDomain();
        when(organisationalUnitService.getAgencyToken(anyLong())).thenReturn(responseDto);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/123/agencyToken")
                        .accept(APPLICATION_JSON))
                .andExpect(jsonPath("$.token", equalTo(responseDto.getToken())))
                .andExpect(jsonPath("$.capacity", equalTo(responseDto.getCapacity())))
                .andExpect(jsonPath("$.capacityUsed", equalTo(responseDto.getCapacityUsed())))
                .andExpect(jsonPath("$.agencyDomains", hasSize(1)))
                .andExpect(jsonPath("$.agencyDomains[0].domain", equalTo(expectedDomainName)))
                .andExpect(status().isOk());

        verify(organisationalUnitService, times(1)).getAgencyToken(eq(123l));
    }

    @Test
    public void shouldThrowTokenDoesNotExistIfNoTokenFound() throws Exception {
        when(organisationalUnitService.getAgencyToken(anyLong())).thenThrow(new TokenDoesNotExistException());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/123/agencyToken")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(organisationalUnitService, times(1)).getAgencyToken(eq(123l));
    }

    @Test
    public void shouldThrowGeneralApplicationExceptionIfTechnicalErrorOccurs() throws Exception {
        RuntimeException expectedCause = new RuntimeException();
        when(organisationalUnitService.getAgencyToken(anyLong())).thenThrow(new CSRSApplicationException("something went wrong", expectedCause));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/123/agencyToken")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(organisationalUnitService, times(1)).getAgencyToken(eq(123l));
    }

    @Test
    public void shouldSaveAgencyTokenIfValidAgencyTokenDTOIsProvided() throws Exception {
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        orgUnit.setAbbreviation("NHSDUNDEE");
        orgUnit.setCode("NHSDUN");
        Optional<OrganisationalUnit> orgUnitOptional = Optional.of(orgUnit);

        when(organisationalUnitService.getOrganisationalUnit(anyLong())).thenReturn(orgUnitOptional);
        when(organisationalUnitService.setAgencyToken(eq(orgUnit), any(AgencyToken.class))).thenReturn(orgUnit);

        requestBodyAgencyTokenAsAString = JsonUtils.asJsonString(dto);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/organisationalUnits/123/agencyToken").contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON).content(requestBodyAgencyTokenAsAString))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(organisationalUnitService, times(1)).getOrganisationalUnit(eq(123L));
        verify(organisationalUnitService, times(1)).setAgencyToken(eq(orgUnit), any(AgencyToken.class));
    }

    @Test
    public void shouldNotSaveAgencyTokenIfInvalidAgencyTokenDTOIsProvided_capacityLessThan1() throws Exception {
        // capacity must be between 1 and 1500, this should fail validation
        dto.setCapacity(0);
        requestBodyAgencyTokenAsAString = JsonUtils.asJsonString(dto);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/organisationalUnits/123/agencyToken").contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON).content(requestBodyAgencyTokenAsAString))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(organisationalUnitService, never()).getOrganisationalUnit(anyLong());
        verify(organisationalUnitService, never()).setAgencyToken(any(OrganisationalUnit.class), any(AgencyToken.class));
    }

    @Test
    public void shouldNotSaveAgencyTokenIfInvalidAgencyTokenDTOIsProvided_capacityTooHigh() throws Exception {
        // must be at least 1 domain
        dto.setAgencyDomains(new HashSet<>());
        requestBodyAgencyTokenAsAString = JsonUtils.asJsonString(dto);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/organisationalUnits/123/agencyToken").contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON).content(requestBodyAgencyTokenAsAString))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(organisationalUnitService, never()).getOrganisationalUnit(anyLong());
        verify(organisationalUnitService, never()).setAgencyToken(any(OrganisationalUnit.class), any(AgencyToken.class));
    }

    @Test
    public void deleteAgencyToken_ok() throws Exception {
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setId(500L);

        when(organisationalUnitService.getOrganisationalUnit(organisationalUnit.getId())).thenReturn(Optional.of(organisationalUnit));
        when(organisationalUnitService.deleteAgencyToken(organisationalUnit)).thenReturn(organisationalUnit);

        mockMvc.perform(
                MockMvcRequestBuilders.delete(String.format("/organisationalUnits/%d/agencyToken", organisationalUnit.getId())))
                .andExpect(status().isOk());

        verify(organisationalUnitService, times(1)).getOrganisationalUnit(organisationalUnit.getId());
        verify(organisationalUnitService, times(1)).deleteAgencyToken(organisationalUnit);
    }

    @Test
    public void deleteAgencyToken_error() throws Exception {
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setId(500L);

        when(organisationalUnitService.getOrganisationalUnit(organisationalUnit.getId())).thenReturn(Optional.of(organisationalUnit));
        when(organisationalUnitService.deleteAgencyToken(organisationalUnit)).thenReturn(null);

        mockMvc.perform(
                MockMvcRequestBuilders.delete(String.format("/organisationalUnits/%d/agencyToken", organisationalUnit.getId())))
                .andExpect(status().is5xxServerError());

        verify(organisationalUnitService, times(1)).getOrganisationalUnit(organisationalUnit.getId());
        verify(organisationalUnitService, times(1)).deleteAgencyToken(organisationalUnit);
    }

    @Test
    public void deleteAgencyToken_notFound() throws Exception {
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setId(500L);

        when(organisationalUnitService.getOrganisationalUnit(organisationalUnit.getId())).thenReturn(Optional.empty());

        mockMvc.perform(
                MockMvcRequestBuilders.delete(String.format("/organisationalUnits/%d/agencyToken", organisationalUnit.getId())))
                .andExpect(status().isNotFound());

        verify(organisationalUnitService, times(1)).getOrganisationalUnit(organisationalUnit.getId());
        verify(organisationalUnitService, times(0)).deleteAgencyToken(organisationalUnit);
    }

}
