package uk.gov.cshr.civilservant.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.dto.IdentityAgencyResponseDTO;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.IdentityRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.cshr.civilservant.service.identity.model.AgencyTokenCapacityUsed;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
public class IdentityServiceTest {

    private static final String USER_UID = "myuseruid";

    private static final String IDENTITY_API_URL = "http://localhost/identity";
    private String MAP_FOR_UIDS_URL = "http://localhost:8080/api/api/identities";

    private String AGENCY_TOKEN_URL = "http://localhost:8080/agency/{agencyTokenUid}";

    private String IDENTITY_AGENCY_TOKEN_URL = "http://localhost:8080/identity/agency/";

    private IdentityService identityService;

    private String EXPECTED_AGENCY_TOKEN_URL = "http://localhost:8080/agency/123";

    private String EXPECTED_GET_AGENCY_TOKEN_UID_URL = "http://localhost:8080/identity/agency/" + USER_UID;

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private IdentityRepository identityRepository;

    @Captor
    private ArgumentCaptor<String> uriArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> identityAgencyUrlArgumentCaptor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        initMocks(this);
        identityService = new IdentityService(restClient, IDENTITY_API_URL, AGENCY_TOKEN_URL,
                IDENTITY_AGENCY_TOKEN_URL, identityRepository);
    }

    @Test
    public void shouldReturnFoundIdentities() {

        IdentityDTO identity = new IdentityDTO();
        identity.setUid("uid");
        identity.setUsername("shouldReturnUriStringFromOrg@domain.com");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(IDENTITY_API_URL)
                .queryParam("emailAddress", identity.getUsername());

        when(restClient.get()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec).uri(anyString());

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(IdentityDTO.class)).thenReturn(identity);

        IdentityDTO foundIdentity = identityService.findByEmail(identity.getUsername());

        assertThat(foundIdentity.getUsername(),
                equalTo("shouldReturnUriStringFromOrg@domain.com"));

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(builder.build().toUriString());
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).body(IdentityDTO.class);
    }

    @Test
    public void shouldNotReturnNotFoundIdentities() {

        IdentityDTO identity = new IdentityDTO();
        identity.setUid("uid");
        identity.setUsername("shouldReturnUriStringFromOrg@domain.com");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(IDENTITY_API_URL)
                .queryParam("emailAddress", identity.getUsername());

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        IdentityDTO foundIdentity = identityService.findByEmail(identity.getUsername());
        assertNull(foundIdentity);
    }

    @Test
    public void shouldReturnNumberOfSpacesIfValid()
            throws MalformedURLException, CSRSApplicationException {

        int expectedSpaces = 101;
        String agencyTokenUid = UUID.randomUUID().toString();

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(AGENCY_TOKEN_URL);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec)
                .uri(anyString());

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(AgencyTokenCapacityUsed.class))
                .thenReturn(new AgencyTokenCapacityUsed(expectedSpaces));

        int actual = identityService.getSpacesUsedForAgencyToken(agencyTokenUid);

        assertThat(actual, equalTo(expectedSpaces));

        verify(restClient, times(1)).get();

        verify(requestHeadersUriSpec, times(1))
                .uri(builder.buildAndExpand(agencyTokenUid).toUriString());

        verify(requestHeadersUriSpec, times(1)).retrieve();

        verify(responseSpec, times(1))
                .body(AgencyTokenCapacityUsed.class);
    }

    @Test(expected = TokenDoesNotExistException.class)
    public void shouldThrowNotFoundExceptionIfNoAgencyTokenFound()
            throws MalformedURLException, CSRSApplicationException {

        HttpClientErrorException notFoundHttpClientException =
                new HttpClientErrorException(HttpStatus.NOT_FOUND);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec)
                .uri(anyString());

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(AgencyTokenCapacityUsed.class))
                .thenThrow(notFoundHttpClientException);

        identityService.getSpacesUsedForAgencyToken("123");

        verify(restClient).get();

        verify(requestHeadersUriSpec)
                .uri(uriArgumentCaptor.capture());

        verify(requestHeadersUriSpec).retrieve();

        verify(responseSpec).body(AgencyTokenCapacityUsed.class);

        URI actualURI = URI.create(uriArgumentCaptor.getValue());

        assertThat(actualURI.toURL().toString(), equalTo(EXPECTED_AGENCY_TOKEN_URL));
    }

    @Test (expected = CSRSApplicationException.class)
    public void shouldThrowGeneralApplicationExceptionIfUnexpectedError() throws MalformedURLException, CSRSApplicationException {
        // given
        HttpServerErrorException httpServerException = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(IdentityAgencyResponseDTO.class)).thenThrow(httpServerException);

        // when
        int actual = identityService.getSpacesUsedForAgencyToken("123");

        // then
        assertNull(actual);
        verify(restClient, times(1)).get().uri(uriArgumentCaptor.capture()).retrieve().toEntity(AgencyTokenCapacityUsed.class);

        String actualURI = uriArgumentCaptor.getValue();
        assertThat(actualURI.toString(), equalTo(EXPECTED_AGENCY_TOKEN_URL));
    }

    @Test
    public void getAgencyTokenUid_ok() throws CSRSApplicationException {
        IdentityAgencyResponseDTO response = new IdentityAgencyResponseDTO();
        response.setAgencyTokenUid("100");
        response.setUid(USER_UID);

        ResponseEntity<IdentityAgencyResponseDTO> responseEntity =
                new ResponseEntity<>(response, HttpStatus.OK);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec).uri(anyString());
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toEntity(IdentityAgencyResponseDTO.class))
                .thenReturn(responseEntity);

        Optional<String> actual = identityService.getAgencyTokenUid(USER_UID);

        assertThat(actual.get(), equalTo("100"));

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(uriArgumentCaptor.capture());
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).toEntity(IdentityAgencyResponseDTO.class);

        String actualURL = uriArgumentCaptor.getValue();
        assertThat(actualURL, equalTo(EXPECTED_GET_AGENCY_TOKEN_UID_URL));
    }

    @Test
    public void getAgencyTokenUid_ok_whenNoAgencyTokenUid_shouldTreatAsOptionalEmpty()
            throws CSRSApplicationException {

        IdentityAgencyResponseDTO response = new IdentityAgencyResponseDTO();
        response.setUid(USER_UID);

        ResponseEntity<IdentityAgencyResponseDTO> responseEntity =
                new ResponseEntity<>(response, HttpStatus.OK);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec).uri(anyString());

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toEntity(IdentityAgencyResponseDTO.class))
                .thenReturn(responseEntity);

        Optional<String> actual = identityService.getAgencyTokenUid(USER_UID);

        assertTrue(!actual.isPresent());

        verify(restClient, times(1)).get();

        verify(requestHeadersUriSpec, times(1))
                .uri(identityAgencyUrlArgumentCaptor.capture());

        verify(requestHeadersUriSpec, times(1)).retrieve();

        verify(responseSpec, times(1))
                .toEntity(IdentityAgencyResponseDTO.class);

        String actualURL = identityAgencyUrlArgumentCaptor.getValue();
        assertThat(actualURL, equalTo(EXPECTED_GET_AGENCY_TOKEN_UID_URL));
    }

    @Test
    public void getAgencyTokenUid_notFound() throws CSRSApplicationException {

        ResponseEntity<IdentityAgencyResponseDTO> responseEntity =
                new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec)
                .uri(anyString());

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toEntity(IdentityAgencyResponseDTO.class))
                .thenReturn(responseEntity);

        Optional<String> actual = identityService.getAgencyTokenUid(USER_UID);

        assertTrue(!actual.isPresent());

        verify(restClient).get();

        verify(requestHeadersUriSpec)
                .uri(identityAgencyUrlArgumentCaptor.capture());

        verify(requestHeadersUriSpec).retrieve();

        verify(responseSpec).toEntity(IdentityAgencyResponseDTO.class);

        String actualURL = identityAgencyUrlArgumentCaptor.getValue();

        assertThat(actualURL, equalTo(EXPECTED_GET_AGENCY_TOKEN_UID_URL));
    }

    @Test
    public void getAgencyTokenUid_technicalError() throws CSRSApplicationException {
        RuntimeException runtimeException = new RuntimeException("broken");
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(IdentityAgencyResponseDTO.class)).thenThrow(runtimeException);

        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectCause(instanceOf(RuntimeException.class));
        expectedException.expectMessage("Unexpected error calling identity service: get agency token uid");

        Optional<String> actual = identityService.getAgencyTokenUid(USER_UID);

        assertTrue(!actual.isPresent());
        verify(restClient).get();
        verify(requestHeadersUriSpec)
                .uri(identityAgencyUrlArgumentCaptor.capture());
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).toEntity(IdentityAgencyResponseDTO.class);

        String actualURL = identityAgencyUrlArgumentCaptor.getValue();
        assertThat(actualURL, equalTo(EXPECTED_GET_AGENCY_TOKEN_UID_URL));
    }

    @Test
    public void removeAgencyTokenFromUsers_ok() throws CSRSApplicationException {
        String agencyTokenUid = UUID.randomUUID().toString();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(AGENCY_TOKEN_URL);

        when(restClient.delete()).thenReturn(requestHeadersUriSpec);

        doReturn(requestHeadersUriSpec)
                .when(requestHeadersUriSpec)
                .uri(anyString());

        identityService.removeAgencyTokenFromUsers(agencyTokenUid);

        verify(restClient, times(1)).delete();

        verify(requestHeadersUriSpec, times(1))
                .uri(builder.buildAndExpand(agencyTokenUid).toUriString());
    }

    @Test
    public void removeAgencyTokenFromUsers_clientError() throws CSRSApplicationException {
        String agencyTokenUid = UUID.randomUUID().toString();

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(AGENCY_TOKEN_URL);

        String expectedUrl = builder
                .buildAndExpand(agencyTokenUid)
                .toUriString();

        HttpClientErrorException causeException =
                new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        when(restClient.delete()).thenReturn(requestHeadersUriSpec);

        doThrow(causeException)
                .when(requestHeadersUriSpec)
                .uri(expectedUrl);

        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectMessage(
                "Error calling identity service: delete agency token");
        expectedException.expectCause(instanceOf(HttpClientErrorException.class));

        identityService.removeAgencyTokenFromUsers(agencyTokenUid);

        verify(restClient, times(1)).delete();
        verify(requestHeadersUriSpec, times(1)).uri(expectedUrl);
    }

    @Test
    public void removeAgencyTokenFromUsers_serverError() throws CSRSApplicationException {
        String agencyTokenUid = UUID.randomUUID().toString();

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(AGENCY_TOKEN_URL);

        HttpServerErrorException causeException =
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restClient.delete()).thenReturn(requestHeadersUriSpec);

        doThrow(causeException)
                .when(requestHeadersUriSpec)
                .uri(builder.buildAndExpand(agencyTokenUid).toUriString());

        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectMessage(
                "Server error calling identity service: delete agency token"
        );
        expectedException.expectCause(instanceOf(HttpServerErrorException.class));

        identityService.removeAgencyTokenFromUsers(agencyTokenUid);

        verify(restClient).delete();

        verify(requestHeadersUriSpec)
                .uri(builder.buildAndExpand(agencyTokenUid).toUriString());
    }

    @Test
    public void removeAgencyTokenFromUsers_unknownError() throws CSRSApplicationException {
        String agencyTokenUid = UUID.randomUUID().toString();

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(AGENCY_TOKEN_URL);

        String expectedUrl = builder
                .buildAndExpand(agencyTokenUid)
                .toUriString();

        RuntimeException causeException = new RuntimeException("Unknown error");

        when(restClient.delete()).thenReturn(requestHeadersUriSpec);

        doThrow(causeException)
                .when(requestHeadersUriSpec)
                .uri(expectedUrl);

        expectedException.expect(CSRSApplicationException.class);
        expectedException.expectMessage(
                "Unexpected error calling identity service: delete agency token");
        expectedException.expectCause(instanceOf(RuntimeException.class));

        identityService.removeAgencyTokenFromUsers(agencyTokenUid);

        verify(restClient, times(1)).delete();
        verify(requestHeadersUriSpec, times(1)).uri(expectedUrl);
    }

}
