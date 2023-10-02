package uk.gov.cshr.civilservant.controller.v2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.civilservant.controller.CSRSControllerTestBase;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class OrganisationalUnitControllerTest extends CSRSControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetOrganisationalUnit() throws Exception {
        mockMvc.perform(
                get("/v2/organisationalUnits/4")
                        .with(csrf())
        ).andExpect(jsonPath("parentId").isEmpty())
                .andExpect(jsonPath("parent").isEmpty())
                .andExpect(jsonPath("id").value(4))
                .andExpect(jsonPath("name").value("DH Core"))
                .andExpect(jsonPath("code").value("D21"))
                .andExpect(jsonPath("abbreviation").value("DHC"))
                .andExpect(jsonPath("domains[0].domain").value("some-domain.com"));
    }

    @Test
    public void testGetOrganisationalUnitWithParent() throws Exception {
        mockMvc.perform(
                        get("/v2/organisationalUnits/4")
                                .with(csrf())
                                .param("includeParents", "true")
                ).andExpect(jsonPath("parentId").value("2"))
                .andExpect(jsonPath("parent.id").value(2))
                .andExpect(jsonPath("id").value(4))
                .andExpect(jsonPath("name").value("DH Core"))
                .andExpect(jsonPath("code").value("D21"))
                .andExpect(jsonPath("abbreviation").value("DHC"))
                .andExpect(jsonPath("domains[0].domain").value("some-domain.com"))
                .andExpect(jsonPath("parent.domains[0].domain").value("another-domain.co.uk"))
                .andExpect(jsonPath("parent.domains[1].domain").value("cabinetoffice.gov.uk"));
    }

}
