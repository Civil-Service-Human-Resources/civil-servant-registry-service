package uk.gov.cshr.civilservant.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.service.AgencyTokenService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

@WithMockUser(username = "user")
public class AgencyTokenControllerTest {

    private static final String DOMAIN = "example.com";
    private static final String UID = "UID";
    private static final String TOKEN = "TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockitoBean
    private AgencyTokenService agencyTokenService;

    @Test
    public void shouldReturnTrueIfRequestingAgencyTokensWithDomainIfExists() throws Exception {
        when(agencyTokenService.isDomainInAgency(DOMAIN)).thenReturn(true);

        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?domain=%s", DOMAIN))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

    }

    @Test
    public void shouldReturnFalseIfRequestingAgencyTokensWithDomainIfNotExists() throws Exception {
        when(agencyTokenService.isDomainInAgency(DOMAIN)).thenReturn(false);

        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?domain=%s", DOMAIN))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void shouldReturnOkIfRequestingAgencyTokensWithDomainTokenOrgParams() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        String domain = "example.com";
        String token = "token123";
        String code = "code";

        when(agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, token, code)).thenReturn(Optional.of(agencyToken));

        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?domain=%s&token=%s&code=%s", domain, token, code))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNotFoundIfRequestingAgencyTokensWithDomainTokenOrgParams() throws Exception {
        String domain = "example.com";
        String token = "token123";
        String code = "code";

        when(agencyTokenService.getAgencyTokenByDomainTokenCodeAndOrg(domain, token, code)).thenReturn(Optional.empty());

        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?domain=%s&token=%s&code=%s", domain, token, code))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAgencyTokenIfGetByUidExists() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);
        agencyToken.setToken(TOKEN);

        when(agencyTokenService.getAgencyTokenByUid(UID)).thenReturn(Optional.of(agencyToken));
        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?uid=%s", UID))
                        .with(user("test-user"))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(gson.toJson(agencyToken)));
    }

    @Test
    public void shouldReturnNotFoundIfGetByUidDoesNotExist() throws Exception {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);
        agencyToken.setToken(TOKEN);

        when(agencyTokenService.getAgencyTokenByUid(UID)).thenReturn(Optional.empty());
        mockMvc.perform(
                MockMvcRequestBuilders.get(String.format("/agencyTokens?uid=%s", UID)).with(jwt())
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
