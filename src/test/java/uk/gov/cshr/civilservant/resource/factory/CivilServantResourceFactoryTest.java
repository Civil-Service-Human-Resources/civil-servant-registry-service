package uk.gov.cshr.civilservant.resource.factory;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.OrgCodeDTO;
import uk.gov.cshr.civilservant.resource.CivilServantResource;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CivilServantResourceFactoryTest {
    @Mock
    private IdentityService identityService;

    @Mock
    private LinkFactory linkFactory;

    @InjectMocks
    private CivilServantResourceFactory factory;

    @Test
    public void shouldReturnCivilServantResource() {
        long id = 99L;
        String fullName = "full-name";
        Grade grade = new Grade("code", "name");
        Set<Interest> interests = ImmutableSet.of(new Interest("interest"));
        String lineManagerName = "line-manager";
        String lineManagerEmail = "line-manager@domain.com";
        CivilServant lineManager = new CivilServant();
        lineManager.setFullName(lineManagerName);
        Profession profession = new Profession("profession");
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();

        CivilServant civilServant = new CivilServant();
        civilServant.setId(id);
        civilServant.setFullName(fullName);
        civilServant.setGrade(grade);
        civilServant.setInterests(interests);
        civilServant.setLineManager(lineManager);
        civilServant.setProfession(profession);
        civilServant.setOrganisationalUnit(organisationalUnit);

        when(identityService.getEmailAddress(lineManager)).thenReturn(lineManagerEmail);

        Link selfLink = mock(Link.class);
        when(linkFactory.createSelfLink(civilServant)).thenReturn(selfLink);

        Link organisationLink = mock(Link.class);
        when(linkFactory.createRelationshipLink(civilServant, "organisationalUnit"))
                .thenReturn(organisationLink);

        Link gradeLink = mock(Link.class);
        when(linkFactory.createRelationshipLink(civilServant, "grade"))
                .thenReturn(gradeLink);

        Link professionLink = mock(Link.class);
        when(linkFactory.createRelationshipLink(civilServant, "profession"))
                .thenReturn(professionLink);

        Resource<CivilServantResource> resource = factory.create(civilServant);

        CivilServantResource content = resource.getContent();

        assertTrue(resource.getLinks().contains(selfLink));
        assertTrue(resource.getLinks().contains(organisationLink));
        assertTrue(resource.getLinks().contains(gradeLink));
        assertTrue(resource.getLinks().contains(professionLink));

        assertEquals(fullName, content.getFullName());
        assertEquals(grade.getCode(), content.getGrade().getCode());
        assertEquals(grade.getName(), content.getGrade().getName());
        assertEquals(interests, content.getInterests());
        assertEquals(lineManagerName, content.getLineManagerName());
        assertEquals(lineManagerEmail, content.getLineManagerEmailAddress());
        assertEquals(organisationalUnit, content.getOrganisationalUnit());
        assertEquals(profession, content.getProfession());
    }

    @Test
    public void givenOrgUnitAndOrgUnitCodeExists_whenGetCivilServantOrganisationalUnitCode_thenOrgCodeShouldReturnOrgCode(){
        CivilServant civilServant = buildCivilServant();

        Optional<OrgCodeDTO> actual = factory.getCivilServantOrganisationalUnitCode(civilServant);

        assertThat(actual.get().getCode()).isEqualTo("co");
    }

    @Test
    public void givenNoOrgUnitCodeExists_whenGetCivilServantOrganisationalUnitCode_thenOrgCodeShouldReturnEmptyString(){
        CivilServant civilServant = buildCivilServant();
        civilServant.getOrganisationalUnit().get().setCode(null);

        Optional<OrgCodeDTO> actual = factory.getCivilServantOrganisationalUnitCode(civilServant);

        assertThat(actual).isEmpty();
    }

    @Test
    public void givenNoOrgUnit_whenGetCivilServantOrganisationalUnitCode_thenOrgCodeShouldReturnEmptyString(){
        CivilServant civilServant = buildCivilServant();
        civilServant.setOrganisationalUnit(null);

        Optional<OrgCodeDTO> actual = factory.getCivilServantOrganisationalUnitCode(civilServant);

        assertThat(actual).isEmpty();
    }

    private CivilServant buildCivilServant() {
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode("co");

        CivilServant civilServant = new CivilServant();
        civilServant.setOrganisationalUnit(organisationalUnit);
        return civilServant;
    }
}
