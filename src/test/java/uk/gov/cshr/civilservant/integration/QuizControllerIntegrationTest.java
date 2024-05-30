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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IntegrationTestUserConfig.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class QuizControllerIntegrationTest extends BaseIntegrationTest {

    private final Authentication learner = new CustomAuthentication(Collections.singletonList(new SimpleGrantedAuthority("LEARNING_MANAGER")), "learner");

    @Test
    public void testCreateQuiz() throws Exception {
        mockMvc.perform(
                        post("/api/quiz")
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content("{\"profession\": {\"id\":  \"100\"}}")
                                .with(authentication(learner)))
                .andExpect(status().isOk());
    }
}
