package uk.gov.cshr.civilservant.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@WithMockUser(username = "user", roles = {"ORGANISATION_MANAGER"})
public class OrganisationalUnitRepositoryTest {

    @Autowired
    private OrganisationalUnitRepository repository;

    @Test
    public void shouldFindOrganisationsWhereNameStartsWith() {
        Optional<OrganisationalUnit> organisationalUnit = repository.findByCode("co");
        assertThat(organisationalUnit.get().getName(), is("Cabinet Office"));
    }

    @Test
    public void shouldReturnParent() {
        OrganisationalUnit parent = new OrganisationalUnit();
        parent.setCode("a");
        parent.setName("Parent");

        repository.save(parent);

        OrganisationalUnit child = new OrganisationalUnit();
        child.setCode("b");
        child.setName("Child");
        child.setParent(parent);

        repository.save(child);

        Optional<OrganisationalUnit> savedChild = repository.findByCode("b");

        assertThat(savedChild.get().getParent().getCode(), is("a"));
    }

    @Test
    public void shouldReturnChildren() {
        OrganisationalUnit child1 = new OrganisationalUnit();
        child1.setCode("b");
        child1.setName("Child 1");

        OrganisationalUnit child2 = new OrganisationalUnit();
        child2.setCode("c");
        child2.setName("Child 2");

        OrganisationalUnit parent = new OrganisationalUnit();
        parent.setCode("a");
        parent.setName("Parent");
        parent.setChildren(Arrays.asList(child1, child2));

        repository.save(parent);

        Optional<OrganisationalUnit> foundParent = repository.findByCode("a");

        List<OrganisationalUnit> subOrgs = new ArrayList<>(foundParent.get().getChildren());

        assertThat(subOrgs.size(), is(2));
        assertThat(subOrgs.get(0).getCode(), is("b"));
        assertThat(subOrgs.get(1).getCode(), is("c"));
    }

    @Test
    public void shouldConvertListOfPaymentMethodsToStringAndBack() {

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setPaymentMethods(Arrays.asList("method1", "method2", "method3"));
        organisationalUnit.setName("name");
        organisationalUnit.setCode("xx");

        repository.save(organisationalUnit);

        Optional<OrganisationalUnit> result = repository.findByCode("xx");

        assertThat(result.get().getPaymentMethods(), is(Arrays.asList("method1", "method2", "method3")));
    }
}