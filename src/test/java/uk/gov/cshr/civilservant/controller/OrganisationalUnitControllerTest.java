package uk.gov.cshr.civilservant.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyTokenDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.exception.NoOrganisationsFoundException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.service.CivilServantService;
import uk.gov.cshr.civilservant.service.OrganisationalUnitService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;
import uk.gov.cshr.civilservant.utils.JsonUtils;
import uk.gov.cshr.civilservant.utils.MockMVCFilterOverrider;
import uk.gov.cshr.civilservant.utils.OrganisationalUnitTestUtils;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class OrganisationalUnitControllerTest {

    private static final String WL_DOMAIN = "mydomain.com";
    private static final String NHS_GLASGOW_DOMAIN = "nhsglasgow.gov.uk";
    private static final String UID = "myuid";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganisationalUnitService organisationalUnitService;

    @MockBean
    private CivilServantService civilServantService;

    private String requestBodyAgencyTokenAsAString;

    private AgencyTokenDTO dto;

    private List<OrganisationalUnit> completeList;

    private List<OrganisationalUnit> filteredList;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException, CSRSApplicationException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
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
        when(organisationalUnitService.getOrganisationsForDomain(eq(WL_DOMAIN), eq(UID))).thenReturn(completeList);
        when(organisationalUnitService.getOrganisationsForDomain(eq(NHS_GLASGOW_DOMAIN), eq(UID))).thenReturn(filteredList);
    }

    @Test
    public void shouldReturnOkIfRequestingOrganisationalUnitTree() throws Exception {
        ArrayList<OrganisationalUnit> organisationalUnits = new ArrayList<>();

        when(organisationalUnitService.getOrgTree()).thenReturn(organisationalUnits);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/tree")
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnOkIfRequestingOrganisationalUnitFlat() throws Exception {
        List<OrganisationalUnitDto> organisationalUnitsList = new ArrayList<>();

        when(organisationalUnitService.getFlatOrg()).thenReturn(organisationalUnitsList);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat")
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnOkIfRequestingOrganisationalListFilteredByDomain_wlDomain() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(completeList.size())))
                .andExpect(jsonPath("$[0].code", equalTo(completeList.get(0).getCode())))
                .andExpect(jsonPath("$[9].code", equalTo(completeList.get(9).getCode())));
    }

    @Test
    public void shouldReturnOkIfRequestingOrganisationalListFilteredByDomain_agencyDomain() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + NHS_GLASGOW_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(filteredList.size())))
                .andExpect(jsonPath("$[0].code", equalTo(filteredList.get(0).getCode())))
                .andExpect(jsonPath("$[2].code", equalTo(filteredList.get(2).getCode())));
    }

    @Test
    public void shouldReturn200IfRequestingNonExistentCivilServant() throws Exception {
        when(civilServantService.getCivilServantUid()).thenThrow(new CivilServantNotFoundException());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn200IfRequestingNonExistentAgencyToken() throws Exception {
        when(organisationalUnitService.getOrganisationsForDomain(eq(WL_DOMAIN), eq(UID))).thenThrow(new TokenDoesNotExistException());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn200IfRequestingNonExistentOrganisation() throws Exception {
        when(organisationalUnitService.getOrganisationsForDomain(eq(WL_DOMAIN), eq(UID))).thenThrow(new NoOrganisationsFoundException(WL_DOMAIN));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn500IfGeneralApplicationError() throws Exception {
        when(organisationalUnitService.getOrganisationsForDomain(eq(WL_DOMAIN), eq(UID))).thenThrow(new CSRSApplicationException("broken", new RuntimeException()));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldReturn500IfTechnicalError() throws Exception {
        when(organisationalUnitService.getOrganisationsForDomain(eq(WL_DOMAIN), eq(UID))).thenThrow(new RuntimeException());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/flat/" + WL_DOMAIN + "/")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
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
    public void removeParentOrganisationReturnsOKWhenRun() throws Exception {
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setId(30L);

        when(organisationalUnitService.getOrganisationalUnit(organisationalUnit.getId())).thenReturn(Optional.of(organisationalUnit));
        when(organisationalUnitService.deleteAgencyToken(organisationalUnit)).thenReturn(organisationalUnit);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(String.format("/organisationalUnits/%d/parent", organisationalUnit.getId())))
                .andExpect(status().isOk());

        verify(organisationalUnitService, times(1)).getOrganisationalUnit(organisationalUnit.getId());
        verify(organisationalUnitService, times(1)).removeParentOrganisation(organisationalUnit);
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

    @Test
    public void shouldReturnOkIfRequestingAllCodesMap2() throws Exception {
        String code1 = "code1";
        String code2 = "code2";
        List<String> organisationalUnitsCodesList = Arrays.asList(code1, code2);

        OrganisationalUnit organisationalUnit1 = new OrganisationalUnit();
        organisationalUnit1.setCode(code1);

        OrganisationalUnit organisationalUnit2 = new OrganisationalUnit();
        organisationalUnit2.setCode(code2);

        OrganisationalUnit organisationalUnit3 = new OrganisationalUnit();
        organisationalUnit3.setCode(code1);

        OrganisationalUnit organisationalUnit4 = new OrganisationalUnit();
        organisationalUnit4.setCode(code2);

        List<OrganisationalUnit> organisationalUnitsParentsList1 = Arrays.asList(organisationalUnit1, organisationalUnit2);
        List<OrganisationalUnit> organisationalUnitsParentsList2 = Arrays.asList(organisationalUnit3, organisationalUnit4);

        when(organisationalUnitService.getOrganisationalUnitCodes()).thenReturn(organisationalUnitsCodesList);

        when(organisationalUnitService.getOrganisationWithParents(code1)).thenReturn(organisationalUnitsParentsList1);
        when(organisationalUnitService.getOrganisationWithParents(code2)).thenReturn(organisationalUnitsParentsList2);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/organisationalUnits/allCodesMap")
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
