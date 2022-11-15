package uk.gov.cshr.civilservant.controller.v2;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.utils.MockMVCFilterOverrider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class OrganisationalUnitControllerV2Test {

    private final List<OrganisationalUnit> orgs = new ArrayList<>();

    public OrganisationalUnitControllerV2Test() {
        OrganisationalUnit grandparent = new OrganisationalUnit("grandparent", "GP001", "GPT");
        grandparent.setId(1L);
        OrganisationalUnit parent = new OrganisationalUnit("parent", "P001", "PT");
        parent.setId(2L);
        OrganisationalUnit child = new OrganisationalUnit("child", "C001", "CH");
        child.setId(3L);
        OrganisationalUnit grandparentTwo = new OrganisationalUnit("grandparentTwo", "GP002", "GPT2");
        grandparentTwo.setId(4L);

        child.setParent(parent);
        parent.setParent(grandparent);

        orgs.add(grandparent);
        orgs.add(parent);
        orgs.add(child);
        orgs.add(grandparentTwo);

    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrganisationalUnitRepository organisationalUnitRepository;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy" );
    }

    @Test
    @SneakyThrows
    public void testGetOrganisations() {
        when(organisationalUnitRepository.findAll()).thenReturn(orgs);
        mockMvc.perform(get("/v2/organisationalUnits")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationalUnits[0].name").value("child"))
                .andExpect(jsonPath("$.organisationalUnits[1].name").value("grandparent"))
                .andExpect(jsonPath("$.organisationalUnits[2].name").value("grandparentTwo"))
                .andExpect(jsonPath("$.organisationalUnits[3].name").value("parent"));
    }

    @Test
    @SneakyThrows
    public void testGetOrganisation() {
        when(organisationalUnitRepository.findById(1L)).thenReturn(Optional.of(orgs.get(0)));
        mockMvc.perform(get("/v2/organisationalUnits/1")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("grandparent"));
    }

    @Test
    @SneakyThrows
    public void testGetOrganisationAndParents() {
        when(organisationalUnitRepository.findById(2L)).thenReturn(Optional.of(orgs.get(1)));
        mockMvc.perform(get("/v2/organisationalUnits/2?includeParents=true")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("parent"))
                .andExpect(jsonPath("$.parent.name").value("grandparent"));
    }
}
