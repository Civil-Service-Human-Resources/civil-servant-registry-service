package uk.gov.cshr.civilservant.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import uk.gov.cshr.civilservant.controller.CSRSControllerTestBase;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class BaseIntegrationTest extends CSRSControllerTestBase {

    protected static final WireMockServer wireMockServer =
            new WireMockServer(options()
                    .port(9000)
                    .notifier(new ConsoleNotifier(true)));

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        configureFor("localhost", 9000);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }
}