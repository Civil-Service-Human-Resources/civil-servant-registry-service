package uk.gov.cshr.civilservant.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyDomainDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.factory.AgencyTokenResponseDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.AgencyTokenRepository;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyTokenServiceTest {
    private static final String EXAMPLE_DOMAIN = "example.com";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AgencyTokenRepository agencyTokenRepository;

    @Mock
    private OrganisationalUnitRepository organisationalUnitRepository;

    @Mock
    private AgencyTokenResponseDtoFactory agencyTokenResponseDtoFactory;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private AgencyTokenService agencyTokenService;

    @Test
    public void getAllAgencyTokensByDomain() {
        List<AgencyToken> agencyTokens = new ArrayList<>();
        String domain = "example.com";

        when(agencyTokenRepository.findAllByDomain(domain)).thenReturn(agencyTokens);

        assertEquals(agencyTokens, agencyTokenService.getAllAgencyTokensByDomain(domain));
    }

    @Test
    public void getAgencyTokenByDomainAndToken() {
        String token = "token123";
        String domain = "example.com";

        AgencyToken agencyToken = new AgencyToken();

        Optional<AgencyToken> optionalAgencyToken = Optional.of(agencyToken);

        when(agencyTokenRepository.findByDomainAndToken(domain, token)).thenReturn(optionalAgencyToken);

        assertEquals(optionalAgencyToken, agencyTokenService.getAgencyTokenByDomainAndToken(domain, token));
    }

    @Test
    public void givenCodeIsOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnAgencyToken() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit organisationalUnit = new OrganisationalUnit("org-name", "org-code", "org-abbrv");

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(organisationalUnit));

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), organisationalUnit.getCode());

        assertTrue(returnedToken.isPresent());
        assertEquals(agencyToken, returnedToken.get());
    }

    @Test
    public void givenCodeHasChildOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnAgencyToken() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit("parent-name", "parent-code", "parent-abbrv");
        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit("child-name", "child-code", "child-abbrv");
        parentOrganisationalUnit.setChildren(Collections.singletonList(childOrganisationalUnit));

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(parentOrganisationalUnit));

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), childOrganisationalUnit.getCode());

        assertTrue(returnedToken.isPresent());
        assertEquals(agencyToken, returnedToken.get());
    }

    @Test
    public void givenCodeHasGrandchildOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnAgencyToken() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit("parent-name", "parent-code", "parent-abbrv");
        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit("child-name", "child-code", "child-abbrv");
        OrganisationalUnit grandchildOrganisationalUnit = new OrganisationalUnit("grandchild-name", "grandchild-code", "grandchild-abbrv");

        parentOrganisationalUnit.setChildren(Collections.singletonList(childOrganisationalUnit));
        childOrganisationalUnit.setChildren(Collections.singletonList(grandchildOrganisationalUnit));

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(parentOrganisationalUnit));

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), grandchildOrganisationalUnit.getCode());

        assertTrue(returnedToken.isPresent());
        assertEquals(agencyToken, returnedToken.get());
    }

    @Test
    public void givenCodeHasSiblingGrandchildOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnAgencyToken() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit("parent-name", "parent-code", "parent-abbrv");
        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit("child-name", "child-code", "child-abbrv");
        OrganisationalUnit grandchildOneOrganisationalUnit = new OrganisationalUnit("grandchild-one-name", "grandchild-one-code", "grandchild-1-abbrv");
        OrganisationalUnit grandchildTwoOrganisationalUnit = new OrganisationalUnit("grandchild-two-name", "grandchild-two-code", "grandchild-2-abbrv");

        List<OrganisationalUnit> grandchildList = new ArrayList<>();
        grandchildList.add(grandchildOneOrganisationalUnit);
        grandchildList.add(grandchildTwoOrganisationalUnit);

        parentOrganisationalUnit.setChildren(Collections.singletonList(childOrganisationalUnit));
        childOrganisationalUnit.setChildren(grandchildList);

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(parentOrganisationalUnit));

        Optional<AgencyToken> returnedTokenOne = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), grandchildOneOrganisationalUnit.getCode());
        Optional<AgencyToken> returnedTokenTwo = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), grandchildTwoOrganisationalUnit.getCode());

        assertTrue(returnedTokenOne.isPresent());
        assertEquals(agencyToken, returnedTokenOne.get());

        assertTrue(returnedTokenTwo.isPresent());
        assertEquals(agencyToken, returnedTokenTwo.get());
    }

    @Test
    public void givenTokenDoesntExist_getAgencyTokenByDomainTokenAndOrganisation_returnEmptyOptional() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit organisationalUnit = new OrganisationalUnit("org-name", "org-code", "org-abbrv");

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.empty());

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), organisationalUnit.getCode());

        assertFalse(returnedToken.isPresent());
    }

    @Test
    public void givenCodeIsNotOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnEmptyOptional() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit organisationalUnit = new OrganisationalUnit("org-name", "org-code", "org-abbrv");

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(organisationalUnit));

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), "bad-code");

        assertFalse(returnedToken.isPresent());
    }

    @Test
    public void givenCodeIsNotOwnerNorHasChildOwnerOfTokenAndDomain_getAgencyTokenByDomainTokenAndOrganisation_returnEmptyOptional() {
        String domain = "test.domain";
        AgencyToken agencyToken = new AgencyToken(1, "test-token", 1, "uid");
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit("parent-name", "parent-code", "parent-abbrv");
        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit("child-name", "child-code", "child-abbrv");
        parentOrganisationalUnit.setChildren(Collections.singletonList(childOrganisationalUnit));

        when(agencyTokenRepository.findByDomainAndToken(domain, agencyToken.getToken())).thenReturn(Optional.of(agencyToken));
        when(organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken)).thenReturn(Optional.of(parentOrganisationalUnit));

        Optional<AgencyToken> returnedToken = agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, agencyToken.getToken(), "bad-code");

        assertFalse(returnedToken.isPresent());
    }

    @Test
    public void shouldReturnTrueIfDomainInAgency() {
        when(agencyTokenRepository.existsByDomain(EXAMPLE_DOMAIN)).thenReturn(true);
        assertTrue(agencyTokenService.isDomainInAgency(EXAMPLE_DOMAIN));
    }

    @Test
    public void shouldReturnFalseIfDomainNotInAgency() {
        when(agencyTokenRepository.existsByDomain(EXAMPLE_DOMAIN)).thenReturn(false);
        assertFalse(agencyTokenService.isDomainInAgency(EXAMPLE_DOMAIN));
    }

    @Test
    public void shouldReturnDTOIfValidCapacityUsedAndValidAgencyToken() throws CSRSApplicationException {
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        int expectedCapacityUsed = 5;

        when(identityService.getSpacesUsedForAgencyToken(anyString())).thenReturn(expectedCapacityUsed);
        when(agencyTokenResponseDtoFactory.buildDto(eq(agencyToken), eq(expectedCapacityUsed))).thenCallRealMethod();

        AgencyTokenResponseDto actual = agencyTokenService.getAgencyTokenResponseDto(agencyToken);

        assertThat(actual.getToken(), equalTo((agencyToken.getToken())));
        assertThat(actual.getCapacity(), equalTo((agencyToken.getCapacity())));
        assertThat(actual.getCapacityUsed(), equalTo(expectedCapacityUsed));

        Set<AgencyDomainDTO> actualAgencyDomains = actual.getAgencyDomains();
        assertEquals(actualAgencyDomains.size(), 1);
        AgencyDomainDTO[] actualAgencyDomainsAsAnArray = actualAgencyDomains.toArray(new AgencyDomainDTO[actualAgencyDomains.size()]);
        assertEquals(actualAgencyDomainsAsAnArray[0].getDomain(), AgencyTokenTestingUtils.getExpectedFirstDomainNameFromSetOfAgencyDomains());
    }

    @Test
    public void shouldReturnDTOWithEmptyDomainsIfValidCapacityUsedAndValidAgencyTokenWithNoDomains() throws CSRSApplicationException {
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        agencyToken.getAgencyDomains().clear();
        int expectedCapacityUsed = 5;
        when(identityService.getSpacesUsedForAgencyToken(anyString())).thenReturn(expectedCapacityUsed);
        when(agencyTokenResponseDtoFactory.buildDto(eq(agencyToken), eq(expectedCapacityUsed))).thenCallRealMethod();

        AgencyTokenResponseDto actual = agencyTokenService.getAgencyTokenResponseDto(agencyToken);

        assertThat(actual.getToken(), equalTo((agencyToken.getToken())));
        assertThat(actual.getCapacity(), equalTo((agencyToken.getCapacity())));
        assertThat(actual.getCapacityUsed(), equalTo(expectedCapacityUsed));

        Set<AgencyDomainDTO> actualAgencyDomains = actual.getAgencyDomains();
        assertEquals(actualAgencyDomains.size(), 0);
    }

    @Test
    public void shouldThrowTokenDoesNotExistExceptionIfInvalidAgencyToken() throws CSRSApplicationException {
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        when(identityService.getSpacesUsedForAgencyToken(anyString())).thenThrow(new TokenDoesNotExistException());
        expectedException.expect(TokenDoesNotExistException.class);

        AgencyTokenResponseDto actual = agencyTokenService.getAgencyTokenResponseDto(agencyToken);

        verifyNoInteractions(agencyTokenResponseDtoFactory);
    }

    @Test
    public void shouldThrowGeneralApplicationExceptionIfTechnicalError() throws CSRSApplicationException {
        AgencyToken agencyToken = AgencyTokenTestingUtils.getAgencyToken();
        RuntimeException runtimeException = new RuntimeException();
        when(identityService.getSpacesUsedForAgencyToken(anyString())).thenThrow(new CSRSApplicationException("something went wrong", runtimeException));
        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectMessage("something went wrong");
        expectedException.expectCause(is(runtimeException));

        AgencyTokenResponseDto actual = agencyTokenService.getAgencyTokenResponseDto(agencyToken);

        verifyNoInteractions(agencyTokenResponseDtoFactory);
    }

}
