package uk.gov.cshr.civilservant.service.comparator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingDirection;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingKey;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationalUnitComparatorTest {
    OrganisationalUnitDto orgOne = new OrganisationalUnitDto();
    OrganisationalUnitDto orgTwo = new OrganisationalUnitDto();
    OrganisationalUnitDto orgThree = new OrganisationalUnitDto();

    public OrganisationalUnitComparatorTest() {
        orgOne.setFormattedName("A");
        orgOne.setName("A");
        orgTwo.setFormattedName("F");
        orgTwo.setName("F");
        orgThree.setFormattedName("Z");
        orgThree.setName("Z");
    }

    @Test
    public void testComparatorOrderingDirectionDESC() {
        List<OrganisationalUnitDto> list = Arrays.asList(orgOne, orgTwo, orgThree);

        Comparator<OrganisationalUnitDto> comparator = new OrganisationalUnitComparator(
                OrganisationalUnitOrderingKey.FORMATTED_NAME, OrganisationalUnitOrderingDirection.DESC);

        list.sort(comparator);
        assertEquals(list.get(0).getFormattedName(), "Z");
        assertEquals(list.get(1).getFormattedName(), "F");
        assertEquals(list.get(2).getFormattedName(), "A");
    }

    @Test
    public void testComparatorOrderingDirectionASC() {
        List<OrganisationalUnitDto> list = Arrays.asList(orgThree, orgOne, orgTwo);

        Comparator<OrganisationalUnitDto> comparator = new OrganisationalUnitComparator(
                OrganisationalUnitOrderingKey.FORMATTED_NAME, OrganisationalUnitOrderingDirection.ASC);

        list.sort(comparator);
        assertEquals(list.get(0).getFormattedName(), "A");
        assertEquals(list.get(1).getFormattedName(), "F");
        assertEquals(list.get(2).getFormattedName(), "Z");
    }

    @Test
    public void testComparatorOrderingDirectionNameOrdering() {
        List<OrganisationalUnitDto> list = Arrays.asList(orgThree, orgOne, orgTwo);

        Comparator<OrganisationalUnitDto> comparator = new OrganisationalUnitComparator(
                OrganisationalUnitOrderingKey.NAME, OrganisationalUnitOrderingDirection.ASC);

        list.sort(comparator);
        assertEquals(list.get(0).getName(), "A");
        assertEquals(list.get(1).getName(), "F");
        assertEquals(list.get(2).getName(), "Z");
    }
}
