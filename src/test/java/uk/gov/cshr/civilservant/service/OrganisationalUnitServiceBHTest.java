package uk.gov.cshr.civilservant.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.AgencyDomain;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.NoOrganisationsFoundException;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.cshr.civilservant.utils.AgencyTokenTestingUtils;
import uk.gov.cshr.civilservant.utils.FamilyOrganisationUnits;
import uk.gov.cshr.civilservant.utils.OrganisationalUnitTestUtils;
import uk.gov.cshr.civilservant.utils.TypeList;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationalUnitServiceBHTest {

    private static String GODFATHERS_CODE;

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

    private static List<OrganisationalUnit> ALL_ORGS;

    @BeforeClass
    public static void staticsetUp(){
        ALL_ORGS = new ArrayList<>(10);
        for(int i=0; i<10; i++) {
            ALL_ORGS.add(OrganisationalUnitTestUtils.buildOrgUnit("wl", i, "whitelisted-domain"));
        }
        // ensure all orgs list has matching domains to "mydomain"
        ALL_ORGS.get(5).setAgencyToken(AgencyTokenTestingUtils.createAgencyToken());
        AgencyDomain agencyDomain = new AgencyDomain();
        agencyDomain.setDomain("mydomain");
        agencyDomain.setId(new Long(1));
        AgencyDomain anotherAgencyDomain = new AgencyDomain();
        anotherAgencyDomain.setDomain("mydomain");
        anotherAgencyDomain.setId(new Long(2));
        Set<AgencyDomain> unique = new HashSet<>();
        unique.add(agencyDomain);
        unique.add(anotherAgencyDomain);
        ALL_ORGS.get(5).getAgencyToken().setAgencyDomains(unique);
    }

    @Before
    public void setUp(){
        when(organisationalUnitRepository.findAll()).thenReturn(ALL_ORGS);

        family = new FamilyOrganisationUnits();
        GODFATHERS_CODE = family.getTopParent().getCode();

        // mocking for the top parent - first level
        Optional<OrganisationalUnit> topOrg = Optional.of(family.getTopParent());
        when(organisationalUnitRepository.findByCode(eq(GODFATHERS_CODE))).thenReturn(topOrg);

        // mocking for godfathers children - second level
        for (int i=0; i<family.getTopParent().getChildren().size(); i++) {
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(i));
            if(childAtIndexI.isPresent()) {
                when(organisationalUnitRepository.findByCode(eq(childAtIndexI.get().getCode()))).thenReturn(childAtIndexI);
            }
        }

        // mocking for godfather children, child 1s children - second generation
        for (int i=0; i<family.getTopParent().getChildren().get(1).getChildren().size(); i++) {
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(1).getChildren().get(i));
            if(childAtIndexI.isPresent()) {
                when(organisationalUnitRepository.findByCode(eq(childAtIndexI.get().getCode()))).thenReturn(childAtIndexI);
            }
        }

        // mocking for godfather children, child 2s children - second generation
        for (int i=0; i<family.getTopParent().getChildren().get(2).getChildren().size(); i++) {
            Optional<OrganisationalUnit> childAtIndexI = Optional.of(family.getTopParent().getChildren().get(2).getChildren().get(i));
            if(childAtIndexI.isPresent()) {
                when(organisationalUnitRepository.findByCode(eq(childAtIndexI.get().getCode()))).thenReturn(childAtIndexI);
            }
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
    public void shouldDeleteAgencyToken() {
        AgencyToken agencyToken = new AgencyToken();
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setAgencyToken(agencyToken);

        doNothing().when(agencyTokenService).deleteAgencyToken(agencyToken);

        assertNull(organisationalUnitService.deleteAgencyToken(organisationalUnit));
    }

    @Test
    public void givenAWhitelistedDomain_whenGetOrganisationsForDomain_thenReturnAllOrganisations() {
        // given
        when(identityService.isDomainWhiteListed(anyString())).thenReturn(true);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain("mydomain");

        // then
        assertThat(actual).hasSize(ALL_ORGS.size());
        verify(organisationalUnitRepository, times(1)).findAll();
        verifyZeroInteractions(agencyTokenService);
    }

    @Test
    public void givenDomainWithAgencyTokens_whenGetOrganisationsForDomain_thenReturnOnlyOrganisationsForThatAgencyToken() {
        // given
        when(identityService.isDomainWhiteListed(anyString())).thenReturn(false);
        AgencyToken[] atArray = new AgencyToken[4];
        for(int i=0; i<atArray.length; i++) {
            atArray[i] = AgencyTokenTestingUtils.createAgencyToken();
        }
        Iterable<AgencyToken> it = new TypeList<>(atArray);
        when(agencyTokenService.getAllAgencyTokensByDomain(anyString())).thenReturn(it);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain("mydomain");

        // then
        assertThat(actual).hasSize(1);  // domain was only added to one agency token, see static set up method
        verify(agencyTokenService, times(1)).getAllAgencyTokensByDomain("mydomain");
        verify(organisationalUnitRepository, times(1)).findAll();
    }

    @Test (expected = NoOrganisationsFoundException.class)
    public void givenDomainWithNonWhiteListedDomainAndNoAgencyTokens_whenGetOrganisationsForDomain_thenThrowNoOrganisationsFoundException() {

        /*
        Note: At time of writing, this is a valid scenario.
        as the SOR for agency token is csrs and the SOR for whitelisted domains is identity service
        Therefore there is nothing to stop an admin person adding agency tokens to an whitelisted domain.
         */
        // given
        when(identityService.isDomainWhiteListed(anyString())).thenReturn(false);
        AgencyToken[] atArray = new AgencyToken[0];
        Iterable<AgencyToken> it = new TypeList<>(atArray);
        when(agencyTokenService.getAllAgencyTokensByDomain(anyString())).thenReturn(it);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain("mydomain");

        // then
        assertThat(actual).hasSize(ALL_ORGS.size());
        verify(agencyTokenService, times(1)).getAllAgencyTokensByDomain("mydomain");
        verify(organisationalUnitRepository, times(1)).findAll();
    }

    @Test (expected = NoOrganisationsFoundException.class)
    public void givenDomainWithNonWhiteListedDomainAndNoOrganisations_whenGetOrganisationsForDomain_thenThrowNoOrganisationsFoundException() {

        /*
        Note: At time of writing, this is a valid scenario.
        as the SOR for agency token is csrs and the SOR for whitelisted domains is identity service
        Therefore there is nothing to stop an admin person adding agency tokens to an whitelisted domain.
         */
        // given
        when(identityService.isDomainWhiteListed(anyString())).thenReturn(false);
        AgencyToken[] atArray = new AgencyToken[4];
        for(int i=0; i<atArray.length; i++) {
            atArray[i] = AgencyTokenTestingUtils.createAgencyToken();
        }
        Iterable<AgencyToken> it = new TypeList<>(atArray);
        when(agencyTokenService.getAllAgencyTokensByDomain(anyString())).thenReturn(it);

        // override static set up method
        List<OrganisationalUnit> orgs = new ArrayList<>(10);
        for(int i=0; i<10; i++) {
            orgs.add(OrganisationalUnitTestUtils.buildOrgUnit("wl", i, "whitelisted-domain"));
        }
        when(organisationalUnitRepository.findAll()).thenReturn(orgs);

        // when
        List<OrganisationalUnit> actual = organisationalUnitService.getOrganisationsForDomain("mydomain");

        // then
        assertThat(actual).hasSize(1); // domain was only added to one agency token, see static set up method
        verify(agencyTokenService, times(1)).getAllAgencyTokensByDomain("mydomain");
        verify(organisationalUnitRepository, times(1)).findAll();
    }

}
