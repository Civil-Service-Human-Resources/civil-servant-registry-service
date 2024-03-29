package uk.gov.cshr.civilservant.config;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.repository.IdentityRepository;
import uk.gov.cshr.civilservant.security.CsrsJwtAccessTokenConverter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
@EnableWebSecurity
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private CivilServantRepository civilServantRepository;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/domains").authenticated()
            .anyRequest().permitAll();
    }

    @Bean
    public TokenStore getTokenStore(OAuthProperties oAuthProperties) {
        return new JwtTokenStore(accessTokenConverter(oAuthProperties));
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter(OAuthProperties oAuthProperties) {
        CsrsJwtAccessTokenConverter csrsJwtAccessTokenConverter = new CsrsJwtAccessTokenConverter(identityRepository, civilServantRepository);
        csrsJwtAccessTokenConverter.setSigningKey(oAuthProperties.getJwtKey());
        return csrsJwtAccessTokenConverter;
    }

    @Bean
    public OAuth2ProtectedResourceDetails resourceDetails(OAuthProperties oAuthProperties) {

        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        resource.setId("identity");
        resource.setAccessTokenUri(oAuthProperties.getTokenUrl());
        resource.setClientId(oAuthProperties.getClientId());
        resource.setClientSecret(oAuthProperties.getClientSecret());

        return resource;
    }

    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager(OAuthProperties oAuthProperties) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(oAuthProperties.getMaxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(oAuthProperties.getDefaultMaxConnectionsPerRoute());
        HttpHost host = new HttpHost(oAuthProperties.getServiceUrl());
        connectionManager.setMaxPerRoute(new HttpRoute(host), oAuthProperties.getMaxPerServiceUrl());

        return connectionManager;
    }

    @Bean
    public OAuth2RestOperations oAuthRestTemplate(OAuth2ProtectedResourceDetails resourceDetails, PoolingHttpClientConnectionManager connectionManager) {
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setConnectionManager(connectionManager)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        AccessTokenRequest atr = new DefaultAccessTokenRequest();
        OAuth2RestTemplate oAuthRestTemplate = new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext(atr));
        oAuthRestTemplate.setRequestFactory(requestFactory);

        return oAuthRestTemplate;
    }
}
