package uk.gov.cshr.civilservant.service;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.AgencyDomain;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyDomainDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.NoOrganisationsFoundException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;
import uk.gov.cshr.civilservant.utils.FamilyOrganisationUnits;
import uk.gov.cshr.civilservant.utils.OrganisationalUnitTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationalUnitServiceTest {

    private static String GODFATHERS_CODE;
    private static List<OrganisationalUnit> ALL_ORGS;

    private static final String WL_DOMAIN = "mydomain.com";
    private static final String NHS_GLASGOW_DOMAIN = "nhsglasgow.gov.uk";
    private static final String UID = "myuid";

    @Mock
    private OrganisationalUnitRepository organisationalUnitRepository;
    @Mock
    private OrganisationalUnitDtoFactory organisationalUnitDtoFactory;
    @Mock
    private AgencyTokenService agencyTokenService;
    @Mock
    private IdentityService identityService;
    @InjectMocks
    private OrganisationalUnitService organisationalUnitService;
    private FamilyOrganisationUnits family;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void staticSetUp(){
        ALL_ORGS = new ArrayList<>(10);
        for(int i=0; i<10; i++) {
            ALL_ORGS.add(OrganisationalUnitTestUtils.buildOrgUnit("wl", i, "whitelisted-domain"));
        }
    }

    @Before
    public void setUp() {

        family = new FamilyOrganisationUnits();
        GODFATHERS_CODE = family.getTopParent().getCode();

        // mocking for the top parent
        Optional<OrganisationalUnit> topOrg = Optional.of(family.getTopParent());
        when(organisationalUnitRepository.findByCode(eq(GODFATHERS_CODE))).thenReturn(topOrg);

        // mocking for godfathers children - first generation
        for (int i = 0; i < family.getTopParent().getChildren().size(); i++) {
            String codeOfChildAtIndexI = "god" + i;
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(i));
            when(organisationalUnitRepository.findByCode(eq(codeOfChildAtIndexI))).thenReturn(childAtIndexI);
        }

        // mocking for godfather children, child 1s children - second generation
        for (int i = 0; i < family.getTopParent().getChildren().get(1).getChildren().size(); i++) {
            String codeOfChildAtIndexI = "grandOne" + i;
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(1).getChildren().get(i));
            when(organisationalUnitRepository.findByCode(eq(codeOfChildAtIndexI))).thenReturn(childAtIndexI);
        }

        // mocking for godfather children, child 2s children - second generation
        for (int i = 0; i < family.getTopParent().getChildren().get(2).getChildren().size(); i++) {
            String codeOfChildAtIndexI = "grandTwo" + i;
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(2).getChildren().get(i));
            when(organisationalUnitRepository.findByCode(eq(codeOfChildAtIndexI))).thenReturn(childAtIndexI);
        }

    }

    @Test
    public void shouldReturnParentOrganisationalUnits() {
        OrganisationalUnit parent1 = new OrganisationalUnit();
        OrganisationalUnit child1 = new OrganisationalUnit();
        OrganisationalUnit child2 = new OrganisationalUnit();
        child1.setParent(parent1);
        child2.setParent(child1);

        OrganisationalUnit parent2 = new OrganisationalUnit();

        when(organisationalUnitRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(parent1, child1, child2, parent2));

        List<OrganisationalUnit> result = organisationalUnitService.getParents();

        assertEquals(Arrays.asList(parent1, parent2), result);
    }

    @Test
    public void shouldReturnOrganisationalUnitsAsList() {
        OrganisationalUnit parentOrganisationalUnit = new OrganisationalUnit();
        parentOrganisationalUnit.setName("parent1");
        parentOrganisationalUnit.setCode("p1");

        OrganisationalUnit childOrganisationalUnit = new OrganisationalUnit();
        childOrganisationalUnit.setName("child1");
        childOrganisationalUnit.setCode("c1");
        childOrganisationalUnit.setParent(parentOrganisationalUnit);

        OrganisationalUnit grandchildOrganisationalUnit = new OrganisationalUnit();
        grandchildOrganisationalUnit.setName("grandchild1");
        grandchildOrganisationalUnit.setCode("gc1");
        grandchildOrganisationalUnit.setParent(childOrganisationalUnit);

        List<OrganisationalUnit> organisationalUnits = new ArrayList<>();
        organisationalUnits.add(parentOrganisationalUnit);
        organisationalUnits.add(childOrganisationalUnit);
        organisationalUnits.add(grandchildOrganisationalUnit);

        OrganisationalUnitDto parentOrgUnitDto = new OrganisationalUnitDto();
        parentOrgUnitDto.setName(parentOrganisationalUnit.getName());
        parentOrgUnitDto.setCode(parentOrganisationalUnit.getCode());
        parentOrgUnitDto.setFormattedName("parent1");

        OrganisationalUnitDto childOrgUnitDto = new OrganisationalUnitDto();
        childOrgUnitDto.setName(childOrganisationalUnit.getName());
        childOrgUnitDto.setCode(childOrganisationalUnit.getCode());
        childOrgUnitDto.setFormattedName("parent1 | child1");

        OrganisationalUnitDto grandchildOrgUnitDto = new OrganisationalUnitDto();
        grandchildOrgUnitDto.setName(grandchildOrganisationalUnit.getName());
        grandchildOrgUnitDto.setCode(grandchildOrganisationalUnit.getCode());
        grandchildOrgUnitDto.setFormattedName("parent1 | child1 | grandchild1");

        when(organisationalUnitRepository.findAll()).thenReturn(organisationalUnits);

        when(organisationalUnitDtoFactory.create(parentOrganisationalUnit)).thenReturn(parentOrgUnitDto);
        when(organisationalUnitDtoFactory.create(childOrganisationalUnit)).thenReturn(childOrgUnitDto);
        when(organisationalUnitDtoFactory.create(grandchildOrganisationalUnit)).thenReturn(grandchildOrgUnitDto);

        List<OrganisationalUnitDto> organisationalUnitDtoList = organisationalUnitService.getListSortedByValue();

        assertThat(organisationalUnitDtoList).hasSize(3);
        assertThat(organisationalUnitDtoList.get(0).getName()).isEqualTo("parent1");
        assertThat(organisationalUnitDtoList.get(2).getFormattedName()).isEqualTo("parent1 | child1 | grandchild1");
    }

    @Test
    public void shouldReturnAllOrganisationCodes() {
        List<String> codes = Arrays.asList("code1", "code2");

        when(organisationalUnitRepository.findAllCodes()).thenReturn(codes);

        assertEquals(codes, organisationalUnitService.getOrganisationalUnitCodes());
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndTopLevelIsRequested_whenGetOrganisationWithParents_thenShouldReturnParentOnlyOrgUnits() {
        // given

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithParents(GODFATHERS_CODE);

        // then
        assertThat(actual).hasSize(1);
        assertThat(actual).extracting(OrganisationalUnit::getName)
                .containsOnly("Godfather: the head of the family");
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndSecondLevelIsRequested_whenGetOrganisationWithParents_thenShouldReturnSecondLevelItemRequestedAndItsParentOnlyOrgUnits() {
        // given
        List<OrganisationalUnit> secondLevel = family.getParentsChildren();
        String codeOfSecondLevelWithChildren = secondLevel.get(1).getCode();

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithParents(codeOfSecondLevelWithChildren);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(OrganisationalUnit::getName)
                .containsOnly("Godfather: the head of the family", "child 1 of the godfathers");
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndThirdLevelIsRequested_whenGetOrganisationWithParents_thenShouldReturnThirdLevelItemRequestedAndItsSecondLevelParentAndTheTopParentOnlyOrgUnits() {
        // given
        List<OrganisationalUnit> secondLevelChildOnesChildren = family.getParentsChildrenChildren(1);
        String codeOfThirdLevelOrg = secondLevelChildOnesChildren.get(0).getCode();

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithParents(codeOfThirdLevelOrg);

        // then
        assertThat(actual).hasSize(3);
        assertThat(actual).extracting(OrganisationalUnit::getName)
                .containsOnly("Godfather: the head of the family", "child 1 of the godfathers", "child 0 of the god1");
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndTopParentIsRequested_whenGetOrganisationWithChildren_thenShouldReturnAllThreeGenerationsOfOrgUnits() {
        // given

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithChildren(GODFATHERS_CODE);

        // then
        assertThat(actual).hasSize(16);
        assertThat(actual).extracting(OrganisationalUnit::getName)
                .containsOnly("Godfather: the head of the family",
                        "child 0 of the godfathers",
                        "child 1 of the godfathers",
                        "child 2 of the godfathers",
                        "child 3 of the godfathers",
                        "child 4 of the godfathers",
                        "child 0 of the god1",
                        "child 1 of the god1",
                        "child 2 of the god1",
                        "child 3 of the god1",
                        "child 4 of the god1",
                        "child 0 of the god2",
                        "child 1 of the god2",
                        "child 2 of the god2",
                        "child 3 of the god2",
                        "child 4 of the god2"
                );
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndSecondLevelItemWhichHasNoChildrenIsRequested_whenGetOrganisationWithChildren_thenShouldReturnOrgUnitsCascadingDownOnly() {
        // given
        List<OrganisationalUnit> secondLevel = family.getParentsChildren();
        String codeOfSecondLevelWithNoChildren = secondLevel.get(0).getCode();

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithChildren(codeOfSecondLevelWithNoChildren);

        // then
        assertThat(actual).hasSize(1);
        // should contain only these
        assertThat(actual).extracting((OrganisationalUnit::getName)).containsOnly(
                "child 0 of the godfathers");
    }


    @Test
    public void givenAnOrgWithThreeLevelsAndSecondLevelItemsWhichHasFiveChildrenIsRequested_whenGetOrganisationWithChildren_thenShouldReturnOrgUnitsCascadingDownOnly() {
        // given
        List<OrganisationalUnit> secondLevel = family.getParentsChildren();
        String codeOfSecondLevelWithChildren = secondLevel.get(1).getCode();

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithChildren(codeOfSecondLevelWithChildren);

        // then
        assertThat(actual).hasSize(6);
        // should contain only these
        assertThat(actual).extracting((OrganisationalUnit::getName)).containsOnly(
                "child 1 of the godfathers",
                "child 0 of the god1",
                "child 1 of the god1",
                "child 2 of the god1",
                "child 3 of the god1",
                "child 4 of the god1");
    }

    @Test
    public void givenAnOrgWithThreeLevelsAndThirdLevelIsRequested_whenGetOrganisationWithChildren_thenShouldReturnOrgUnitsCascadingDownOnly() {
        // given
        List<OrganisationalUnit> secondLevelChildOnesChildren = family.getParentsChildrenChildren(1);
        String codeOfThirdLevelOrg = secondLevelChildOnesChildren.get(0).getCode();

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationWithChildren(codeOfThirdLevelOrg);

        // then
        assertThat(actual).hasSize(1);
        // should contain only these
        assertThat(actual).extracting((OrganisationalUnit::getName)).containsOnly(
                "child 0 of the god1");
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
    public void givenAWhitelistedDomain_whenGetOrganisationsForDomain_thenReturnAllOrganisations() {
        // given
        when(agencyTokenService.isDomainInAgency(eq(WL_DOMAIN))).thenReturn(false);
        when(organisationalUnitRepository.findAll()).thenReturn(ALL_ORGS);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain(WL_DOMAIN, UID);

        // then
        assertThat(actual).hasSize(ALL_ORGS.size());
        verify(organisationalUnitRepository, times(1)).findAll();
    }

    @Test
    public void givenAgencyTokenDomain_whenGetOrganisationsForDomain_thenReturnMatchingOrganisationsForThatAgencyTokenIncludingTheirChildrenAndCascadeDownOnly() {
        // given
        when(agencyTokenService.isDomainInAgency(eq(NHS_GLASGOW_DOMAIN))).thenReturn(true);
        Optional<AgencyToken> agencyTokenOptional = Optional.of(AgencyTokenTestingUtils.getAgencyToken());
        when(agencyTokenService.getAgencyTokenByUid(eq(UID))).thenReturn(agencyTokenOptional);
        Optional<OrganisationalUnit> nhsGlasgowWithChildren = buildNHSGlasgowWithChildren();
        when(organisationalUnitRepository.findOrganisationByAgencyToken(eq(agencyTokenOptional.get()))).thenReturn(nhsGlasgowWithChildren);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain(NHS_GLASGOW_DOMAIN, UID);

        // then
        // org for that at and all orgs children should be returned,
        // i.e. Glasgow and its children maryhill and govan ONLY
        assertThat(actual).extracting("code").containsExactlyInAnyOrder("NHSGLASGOW", "NHSMARYHILL", "NHSGOVAN");
    }

    @Test
    public void givenAgencyTokenDomainAndNoAgencyTokenFound_whenGetOrganisationsForDomain_thenThrowTokenDoesNotExistException() {
        // given
        when(agencyTokenService.isDomainInAgency(eq(NHS_GLASGOW_DOMAIN))).thenReturn(true);
        when(agencyTokenService.getAgencyTokenByUid(eq(UID))).thenReturn(Optional.empty());
        expectedException.expect(TokenDoesNotExistException.class);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain(NHS_GLASGOW_DOMAIN, UID);

        // then
        verifyZeroInteractions(organisationalUnitRepository);
    }

    @Test
    public void givenAgencyTokenDomainAndNoOrganisationFound_whenGetOrganisationsForDomain_thenThrowNoOrganisationsFoundException() {
        // given
        when(agencyTokenService.isDomainInAgency(eq(NHS_GLASGOW_DOMAIN))).thenReturn(true);
        Optional<AgencyToken> agencyTokenOptional = Optional.of(AgencyTokenTestingUtils.getAgencyToken());
        when(agencyTokenService.getAgencyTokenByUid(eq(UID))).thenReturn(agencyTokenOptional);
        when(organisationalUnitRepository.findOrganisationByAgencyToken(eq(agencyTokenOptional.get()))).thenReturn(Optional.empty());
        expectedException.expect(NoOrganisationsFoundException.class);
        expectedException.expectMessage("No organisations found for domain: " + NHS_GLASGOW_DOMAIN);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain(NHS_GLASGOW_DOMAIN, UID);

        // then
        verifyZeroInteractions(organisationalUnitRepository);
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

    private Optional<OrganisationalUnit> buildNHSGlasgowWithChildren() {
        // set up at for nhs glasgow
        // AT 1
        AgencyToken at = new AgencyToken();
        at.setId(new Long(100));
        at.setToken("token123");
        at.setAgencyDomains(new HashSet<>());

        AgencyDomain nhsGlasgow = new AgencyDomain();
        nhsGlasgow.setId(new Long(300));
        nhsGlasgow.setDomain("nhsglasgow.gov.uk");
        at.getAgencyDomains().add(nhsGlasgow);

        AgencyDomain nhsEdinburgh = new AgencyDomain();
        nhsEdinburgh.setId(new Long(3001));
        nhsEdinburgh.setDomain("nhsedinburgh.gov.uk");
        at.getAgencyDomains().add(nhsEdinburgh);

        // glasgows children x 2
        OrganisationalUnit maryhillNHS = new OrganisationalUnit();
        maryhillNHS.setCode("NHSMARYHILL");

        OrganisationalUnit govanNHS = new OrganisationalUnit();
        govanNHS.setCode("NHSGOVAN");

        // set up org for nhs glasgow
        OrganisationalUnit greaterGlasgowNHS = new OrganisationalUnit();
        greaterGlasgowNHS.setCode("NHSGLASGOW");
        greaterGlasgowNHS.setAgencyToken(at);

        List<OrganisationalUnit> children = new ArrayList<>();
        children.add(maryhillNHS);
        children.add(govanNHS);
        greaterGlasgowNHS.setChildren(children);

        // set the kids parent to be glasgow
        maryhillNHS.setParent(greaterGlasgowNHS);
        govanNHS.setParent(greaterGlasgowNHS);

        greaterGlasgowNHS.setParent(ALL_ORGS.get(0));

        Optional<OrganisationalUnit> optNhsGlasgow = Optional.of(greaterGlasgowNHS);
        when(organisationalUnitRepository.findByCode(eq("NHSGLASGOW"))).thenReturn(optNhsGlasgow);

        Optional<OrganisationalUnit> optNhsMaryhill = Optional.of(maryhillNHS);
        when(organisationalUnitRepository.findByCode(eq("NHSMARYHILL"))).thenReturn(optNhsMaryhill);

        Optional<OrganisationalUnit> optNhsGovan = Optional.of(govanNHS);
        when(organisationalUnitRepository.findByCode(eq("NHSGOVAN"))).thenReturn(optNhsGovan);

        return Optional.of(greaterGlasgowNHS);
    }

}