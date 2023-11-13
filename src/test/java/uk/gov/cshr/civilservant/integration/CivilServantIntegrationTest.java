package uk.gov.cshr.civilservant.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.controller.CSRSControllerTestBase;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.repository.IdentityRepository;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.junit.Assert.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user", authorities = {"INTERNAL"})
public class CivilServantIntegrationTest extends CSRSControllerTestBase {

    @Autowired
    private CivilServantRepository civilServantRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private OrganisationalUnitRepository organisationalUnitRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void shouldRemoveCivilServantOrganisation() throws Exception {
        Identity csId = new Identity("uid");
        identityRepository.save(csId);
        CivilServant cs = new CivilServant(csId);
        OrganisationalUnit org = new OrganisationalUnit();
        org.setName("name");
        org.setCode("code");
        organisationalUnitRepository.saveAndFlush(org);
        cs.setOrganisationalUnit(org);
        civilServantRepository.save(cs);
        mockMvc.perform(
                        post("/civilServants/resource/uid/remove_organisation")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Query query = entityManager.createNativeQuery("select cs.organisational_unit_id from civil_servant cs");
        assertNull(query.getSingleResult());

    }
}
