package uk.gov.cshr.civilservant.utils.apiStubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import uk.gov.cshr.civilservant.service.identity.IdentityFromService;
import uk.gov.cshr.civilservant.service.identity.model.BatchProcessResponse;
import uk.gov.cshr.civilservant.service.identity.model.RemoveReportingAccessInput;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static uk.gov.cshr.civilservant.utils.JsonUtils.asJsonString;

public class IdentityServiceStub {

    private final static String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiZXhwIjoxNzAxMzQ5NTU3LCJhdXRob3JpdGllcyI6WyJDTElFTlQiXSwianRpIjoiMDAwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMDAwIiwiY2xpZW50X2lkIjoiY2xpZW50In0.RJ8z56PjIoqd1C_8F_M9L5F3K1u_s83IujEcze2h6Zo";
    public static void stubGetIdentityWithUid(String uid, IdentityFromService response) {
        stubFor(
                WireMock.get(urlPathEqualTo("/api/identities"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withQueryParam("uid", equalTo(uid))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(asJsonString(response)))
        );
    }

    public static void stubPostClientToken() {
        stubFor(
                WireMock.post(urlPathEqualTo("/oauth/token"))
                        .withHeader("Authorization", containing("Basic"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\n" +
                                        String.format("    \"access_token\": \"%s\",\n", token) +
                                        "    \"token_type\": \"bearer\",\n" +
                                        "    \"expires_in\": 80000,\n" +
                                        "    \"scope\": \"read write\",\n" +
                                        "    \"jti\": \"00000000-0000-0000-0000-000000000000\"\n" +
                                        "}"))
        );
    }
    
    public static void stubPostRemoveReportingAccess(List<String> uids, BatchProcessResponse response) {
        RemoveReportingAccessInput uidsObj = new RemoveReportingAccessInput(uids);
        stubFor(
                WireMock.post(urlPathEqualTo("/api/identities/remove_reporting_roles"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withRequestBody(equalTo(asJsonString(uidsObj)))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(asJsonString(response)))
        );
    }

    public static void stubGetIdentitiesMap(List<String> uids, Map<String, IdentityFromService> response) {
        String uidsBatchString = String.join(",", uids);
        stubFor(
                WireMock.get(urlPathEqualTo("/api/identities/map-for-uids"))
                        .withHeader("Authorization", equalTo("Bearer " + token))
                        .withQueryParam("uids", equalTo(uidsBatchString))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(asJsonString(response)))
        );
    }
}
