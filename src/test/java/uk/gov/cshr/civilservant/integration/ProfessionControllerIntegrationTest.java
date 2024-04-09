package uk.gov.cshr.civilservant.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.config.IntegrationTestUserConfig;
import uk.gov.cshr.civilservant.utils.CustomAuthentication;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ProfessionControllerIntegrationTest extends BaseIntegrationTest {

    private final Authentication learner = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNER")), "learner");

    @Test
    public void testGetProfessionsAsTree() throws Exception {
        mockMvc.perform(
                        get("/professions/tree")
                                .accept(APPLICATION_JSON)
                                .with(authentication(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Analysis")))
                .andExpect(jsonPath("$[0].children[0].id", equalTo(100)))
                .andExpect(jsonPath("$[0].children[0].name", equalTo("Analysis-child")))
                .andExpect(jsonPath("$[0].children[0].children[0].id", equalTo(102)))
                .andExpect(jsonPath("$[0].children[0].children[0].name", equalTo("Analysis-grandchild")));
    }
}
