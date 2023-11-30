package uk.gov.cshr.civilservant.domain;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrganisationalUnitTest {

    private OrganisationalUnit getTestOrgs() {
        OrganisationalUnit child = new OrganisationalUnit();
        child.setId(3L);
        OrganisationalUnit parent = new OrganisationalUnit();
        parent.setId(2L);
        parent.setChildren(Collections.singletonList(child));
        OrganisationalUnit grandparent = new OrganisationalUnit();
        grandparent.setId(1L);
        grandparent.setChildren(Collections.singletonList(parent));
        return grandparent;
    }

    @Test
    public void testGetFlatHierarchy() {
        OrganisationalUnit org = getTestOrgs();
        List<OrganisationalUnit> list = org.getHierarchyAsFlatList();
        assertEquals(list.size(), 3);
        assertEquals(1L, list.get(0).id.longValue());
        assertEquals(2L, list.get(1).id.longValue());
        assertEquals(3L, list.get(2).id.longValue());
    }

    @Test
    public void testGetFlatDescendants() {
        OrganisationalUnit org = getTestOrgs();
        List<OrganisationalUnit> list = org.getDescendantsAsFlatList();
        assertEquals(list.size(), 2);
        assertEquals(2L, list.get(0).id.longValue());
        assertEquals(3L, list.get(1).id.longValue());
    }
}
