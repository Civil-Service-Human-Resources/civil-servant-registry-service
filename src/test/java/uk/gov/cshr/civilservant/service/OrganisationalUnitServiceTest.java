package uk.gov.cshr.civilservant.service;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AddDomainToOrgResponse;
import uk.gov.cshr.civilservant.dto.AgencyDomainDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.BulkUpdate;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.DomainRepository;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationalUnitServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private OrganisationalUnitRepository organisationalUnitRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private OrganisationalUnitDtoFactory organisationalUnitDtoFactory;
    @Mock
    private AgencyTokenService agencyTokenService;
    @Mock
    private IdentityService identityService;
    @InjectMocks
    private OrganisationalUnitService organisationalUnitService;

    private void stubFindAllOrganisationUnits() {

    }

    @Test
    public void shouldReturnParentOrganisationalUnits() {
        OrganisationalUnit parent1 = new OrganisationalUnit();
        parent1.setName("parent1");
        OrganisationalUnit child1 = new OrganisationalUnit();
        child1.setName("child1");
        OrganisationalUnit child2 = new OrganisationalUnit();
        child2.setName("child2");
        child1.setParent(parent1);
        child2.setParent(child1);

        OrganisationalUnit parent2 = new OrganisationalUnit();
        parent2.setName("parent2");

        when(organisationalUnitRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(parent1, child1, child2, parent2));

        List<OrganisationalUnit> result = organisationalUnitService.getParents();

        assertEquals(Arrays.asList(parent1, parent2), result);
    }

    @Test
    public void deleteAgencyToken_ok() throws CSRSApplicationException {

        String name = "name", code = "code", abbrv = "test", token = "token", agencyTokenCode = UUID.randomUUID().toString();

        AgencyToken agencyToken = new AgencyToken(1, token, 100, agencyTokenCode);
        OrganisationalUnit originalOrganisationalUnit = new OrganisationalUnit(name, code, abbrv);
        originalOrganisationalUnit.setId(500L);
        originalOrganisationalUnit.setAgencyToken(agencyToken);

        OrganisationalUnit clonedOrganisationalUnit = new OrganisationalUnit(originalOrganisationalUnit);

        when(organisationalUnitRepository.save(clonedOrganisationalUnit)).thenReturn(clonedOrganisationalUnit);

        organisationalUnitService.deleteAgencyToken(clonedOrganisationalUnit);

        verify(identityService, times(1)).removeAgencyTokenFromUsers(agencyToken.getUid());
        verify(organisationalUnitRepository, times(1)).save(clonedOrganisationalUnit);
        verify(agencyTokenService, times(1)).deleteAgencyToken(agencyToken);

        assertEquals(originalOrganisationalUnit.getName(), clonedOrganisationalUnit.getName());
        assertEquals(originalOrganisationalUnit.getCode(), clonedOrganisationalUnit.getCode());
        assertEquals(originalOrganisationalUnit.getAbbreviation(), clonedOrganisationalUnit.getAbbreviation());
        assertNotEquals(originalOrganisationalUnit.getAgencyToken(), clonedOrganisationalUnit.getAgencyToken());
        assertNull(clonedOrganisationalUnit.getAgencyToken());

    }

    @Test
    public void deleteAgencyToken_removeAgencyTokenFromUsersException() throws CSRSApplicationException {

        String name = "name", code = "code", abbrv = "test", token = "token", agencyTokenCode = UUID.randomUUID().toString();

        AgencyToken agencyToken = new AgencyToken(1, token, 100, agencyTokenCode);
        OrganisationalUnit originalOrganisationalUnit = new OrganisationalUnit(name, code, abbrv);
        originalOrganisationalUnit.setId(500L);
        originalOrganisationalUnit.setAgencyToken(agencyToken);

        OrganisationalUnit clonedOrganisationalUnit = new OrganisationalUnit(originalOrganisationalUnit);

        doThrow(new CSRSApplicationException("Bad error", new Exception("Root"))).when(identityService).removeAgencyTokenFromUsers(agencyToken.getUid());

        OrganisationalUnit returnedOrganisationalUnit = organisationalUnitService.deleteAgencyToken(clonedOrganisationalUnit);

        verify(identityService, times(1)).removeAgencyTokenFromUsers(agencyToken.getUid());
        verify(organisationalUnitRepository, times(0)).save(clonedOrganisationalUnit);
        verify(agencyTokenService, times(0)).deleteAgencyToken(agencyToken);

        assertNull(returnedOrganisationalUnit);
    }

    @Test
    public void shouldReturnAgencyTokenResponseDtoIfValid() throws CSRSApplicationException {
        AgencyTokenResponseDto responseDto = AgencyTokenTestingUtils.getAgencyTokenResponseDto();
        long orgId = 3l;
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        orgUnit.setAgencyToken(agencyToken);
        Optional<OrganisationalUnit> optionalOrganisationalUnit = Optional.of(orgUnit);
        when(organisationalUnitRepository.findById(eq(orgId))).thenReturn(optionalOrganisationalUnit);
        when(agencyTokenService.getAgencyTokenResponseDto(eq(agencyToken))).thenReturn(responseDto);

        AgencyTokenResponseDto actual = organisationalUnitService.getAgencyToken(orgId);

        Assert.assertThat(actual.getToken(), equalTo((responseDto.getToken())));
        Assert.assertThat(actual.getCapacity(), equalTo((responseDto.getCapacity())));
        Assert.assertThat(actual.getCapacityUsed(), equalTo(responseDto.getCapacityUsed()));

        Set<AgencyDomainDTO> actualAgencyDomains = actual.getAgencyDomains();
        assertEquals(actualAgencyDomains.size(), 1);
        AgencyDomainDTO[] actualAgencyDomainsAsAnArray = actualAgencyDomains.toArray(new AgencyDomainDTO[actualAgencyDomains.size()]);
        assertEquals(actualAgencyDomainsAsAnArray[0].getDomain(), "aDomain");
    }

    @Test
    public void shouldThrowTokenDoesNotExistIfOrganisationNotFound() throws CSRSApplicationException {
        AgencyTokenResponseDto responseDto = AgencyTokenTestingUtils.getAgencyTokenResponseDto();
        long orgId = 3l;
        when(organisationalUnitRepository.findById(eq(orgId))).thenReturn(Optional.empty());

        expectedException.expect(TokenDoesNotExistException.class);

        AgencyTokenResponseDto actual = organisationalUnitService.getAgencyToken(orgId);

        verifyZeroInteractions(agencyTokenService);
    }

    @Test
    public void shouldThrowTokenDoesNotExistIfTokenNotFound() throws CSRSApplicationException {
        AgencyTokenResponseDto responseDto = AgencyTokenTestingUtils.getAgencyTokenResponseDto();
        long orgId = 3l;
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        Optional<OrganisationalUnit> optionalOrganisationalUnit = Optional.of(orgUnit);
        when(organisationalUnitRepository.findById(eq(orgId))).thenReturn(optionalOrganisationalUnit);

        expectedException.expect(TokenDoesNotExistException.class);

        AgencyTokenResponseDto actual = organisationalUnitService.getAgencyToken(orgId);
    }

    @Test
    public void shouldThrowGeneralApplicationExceptionIfTechnicalError() throws CSRSApplicationException {
        AgencyTokenResponseDto responseDto = AgencyTokenTestingUtils.getAgencyTokenResponseDto();
        long orgId = 3l;
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        orgUnit.setAgencyToken(agencyToken);
        Optional<OrganisationalUnit> optionalOrganisationalUnit = Optional.of(orgUnit);
        when(organisationalUnitRepository.findById(eq(orgId))).thenReturn(optionalOrganisationalUnit);

        RuntimeException runtimeException = new RuntimeException();
        when(agencyTokenService.getAgencyTokenResponseDto(eq(agencyToken))).thenThrow(new CSRSApplicationException("something went wrong", runtimeException));
        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectMessage("something went wrong");
        expectedException.expectCause(is(runtimeException));

        AgencyTokenResponseDto actual = organisationalUnitService.getAgencyToken(orgId);

        verifyZeroInteractions(agencyTokenService);
    }

    @Test
    public void shouldAddExistingDomainToSingleOrganisationalUnit() {
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        Optional<OrganisationalUnit> optionalOrganisationalUnit = Optional.of(orgUnit);
        when(organisationalUnitRepository.findById(eq(1L))).thenReturn(optionalOrganisationalUnit);
        Domain domain = new Domain("test.com");
        domain.setId(1L);
        Optional<Domain> optionalDomain = Optional.of(domain);
        when(domainRepository.findDomainByDomain(domain.getDomain())).thenReturn(optionalDomain);

        AddDomainToOrgResponse resp = organisationalUnitService.addDomainToOrganisation(1L, "test.com");
        assertEquals(1L, resp.getPrimaryOrganisationId().longValue());
        assertEquals(1L, resp.getDomain().getId().longValue());
        assertEquals("test.com", resp.getDomain().getDomain());
        assertTrue(resp.getSkippedChildOrganisationIds().isEmpty());
        assertTrue(resp.getUpdatedChildOrganisationIds().isEmpty());

        verify(organisationalUnitRepository, times(1)).saveAndFlush(orgUnit);
    }

    @Test
    public void shouldCreateDomainAndAddToOrganisationalUnitAndDescendants() {
        OrganisationalUnit orgUnit1 = new OrganisationalUnit();
        orgUnit1.setId(1L);
        OrganisationalUnit orgUnit2 = new OrganisationalUnit();
        orgUnit2.setId(2L);
        OrganisationalUnit orgUnit3 = new OrganisationalUnit();
        orgUnit3.setId(3L);
        orgUnit3.setChildren(Arrays.asList(orgUnit2, orgUnit1));
        when(organisationalUnitRepository.findById(orgUnit3.getId())).thenReturn(Optional.of(orgUnit3));
        Domain domain = new Domain("new.com");
        domain.setId(1L);
        when(domainRepository.findDomainByDomain("new.com")).thenReturn(Optional.empty());
        when(domainRepository.save(any())).thenReturn(domain);

        AddDomainToOrgResponse resp = organisationalUnitService.addDomainToOrganisation(orgUnit3.getId(), "new.com");
        assertEquals(3L, resp.getPrimaryOrganisationId().longValue());
        assertEquals(1L, resp.getDomain().getId().longValue());
        assertEquals("new.com", resp.getDomain().getDomain());
        assertTrue(resp.getSkippedChildOrganisationIds().isEmpty());
        assertEquals(2, resp.getUpdatedChildOrganisationIds().size());

        verify(organisationalUnitRepository, times(1)).saveAndFlush(orgUnit3);
        verify(organisationalUnitRepository, times(1)).saveAll(any());
    }

    @Test
    public void shouldAddDomainToOrganisationalUnitsInBulk() {
        Domain domain = new Domain("new.com");
        domain.setId(1L);
        OrganisationalUnit orgUnit = new OrganisationalUnit();
        orgUnit.setId(1L);
        OrganisationalUnit orgUnit2 = new OrganisationalUnit();
        orgUnit2.setId(2L);
        orgUnit2.addDomain(domain);
        OrganisationalUnit orgUnit3 = new OrganisationalUnit();
        orgUnit3.setId(3L);
        List<OrganisationalUnit> orgs = Arrays.asList(orgUnit, orgUnit2, orgUnit3);

        BulkUpdate<OrganisationalUnit> resp = organisationalUnitService.bulkAddDomainToOrganisations(orgs, domain);
        assertEquals(resp.getSkippedIds().size(), 1);
        assertEquals(resp.getUpdatedIds().size(), 2);
        assertEquals(2L, resp.getSkippedIds().get(0).longValue());
        assertEquals(1L, resp.getUpdatedIds().get(0).longValue());
        assertEquals(3L, resp.getUpdatedIds().get(1).longValue());
        verify(organisationalUnitRepository, times(1)).saveAll(any());
    }

    @Test
    public void testGetFormattedNamesMap() {
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit();
        parentOrganisationalUnit.setId(1L);
        parentOrganisationalUnit.setName("parent1");
        parentOrganisationalUnit.setCode("p1");

        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit();
        childOrganisationalUnit.setId(2L);
        childOrganisationalUnit.setName("child1");
        childOrganisationalUnit.setCode("c1");
        childOrganisationalUnit.setParent(parentOrganisationalUnit);

        OrganisationalUnit grandchildOrganisationalUnit = new OrganisationalUnit();
        grandchildOrganisationalUnit.setId(3L);
        grandchildOrganisationalUnit.setName("grandchild1");
        grandchildOrganisationalUnit.setCode("gc1");
        grandchildOrganisationalUnit.setParent(childOrganisationalUnit);

        List<OrganisationalUnit> organisationalUnits = new ArrayList<>();
        organisationalUnits.add(parentOrganisationalUnit);
        organisationalUnits.add(childOrganisationalUnit);
        organisationalUnits.add(grandchildOrganisationalUnit);

        when(organisationalUnitRepository.findAll()).thenReturn(organisationalUnits);

        Map<Long, String> map = organisationalUnitService.getFormattedNamesMap();
        assertEquals(organisationalUnits.size(), map.size());
        assertEquals(parentOrganisationalUnit.getName(), map.get(1L));
        assertEquals("parent1 | child1", map.get(2L));
        assertEquals("parent1 | child1 | grandchild1", map.get(3L));
    }
}
