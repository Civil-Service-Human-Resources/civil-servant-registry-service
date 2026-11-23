package uk.gov.cshr.civilservant.dto.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.CivilServantDto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CivilServantDtoFactoryTest {

    @InjectMocks
    private CivilServantDtoFactory dtoFactory;

    @Test
    public void shouldReturnCivilServantDto() {
        String userId = "uid";
        String organisationName = "test-organisation";
        String professionName = "test-profession";
        String gradeName = "test-grade";
        String userName = "test-user";
        String email = "test@example.com";
        String otherAreaOfWork1 = "other-profession-1";
        String otherAreaOfWork2 = "other-profession-2";

        OrganisationalUnit organisation = new OrganisationalUnit();
        organisation.setName(organisationName);

        CivilServant civilServant = new CivilServant(new Identity(userId));
        civilServant.setProfession(new Profession(professionName));
        civilServant.setOrganisationalUnit(organisation);
        civilServant.setGrade(new Grade("x", gradeName));
        civilServant.setFullName(userName);
        civilServant.setOtherAreasOfWork(new HashSet<>(Arrays.asList(
                new Profession(otherAreaOfWork1), new Profession(otherAreaOfWork2))));

        CivilServantDto dto = dtoFactory.create(civilServant);

        assertEquals(userId, dto.getId());
        assertEquals(organisationName, dto.getOrganisation());
        assertEquals(professionName, dto.getProfession());
        assertEquals(gradeName, dto.getGrade());
        assertEquals(userName, dto.getName());
        assertEquals(Arrays.asList(otherAreaOfWork1, otherAreaOfWork2), dto.getOtherAreasOfWork().stream()
                .sorted().collect(Collectors.toList()));
    }

    @Test
    public void whenNoOrgFlagSet_shouldReturnCivilServantDtoWithForceFlagSetToFalse() {
        String userId = "uid";
        String organisationName = "test-organisation";
        String professionName = "test-profession";
        String gradeName = "test-grade";
        String userName = "test-user";
        String email = "test@example.com";
        String otherAreaOfWork1 = "other-profession-1";
        String otherAreaOfWork2 = "other-profession-2";

        OrganisationalUnit organisation = new OrganisationalUnit();
        organisation.setName(organisationName);

        CivilServant civilServant = new CivilServant(new Identity(userId));
        civilServant.setProfession(new Profession(professionName));
        civilServant.setOrganisationalUnit(organisation);
        civilServant.setGrade(new Grade("x", gradeName));
        civilServant.setFullName(userName);
        civilServant.setOtherAreasOfWork(new HashSet<>(Arrays.asList(
                new Profession(otherAreaOfWork1), new Profession(otherAreaOfWork2))));

        CivilServantDto dto = dtoFactory.create(civilServant);

        assertEquals(userId, dto.getId());
        assertEquals(organisationName, dto.getOrganisation());
        assertEquals(professionName, dto.getProfession());
        assertEquals(gradeName, dto.getGrade());
        assertEquals(userName, dto.getName());
        assertEquals(Arrays.asList(otherAreaOfWork1, otherAreaOfWork2), dto.getOtherAreasOfWork().stream()
                .sorted().collect(Collectors.toList()));
    }
}