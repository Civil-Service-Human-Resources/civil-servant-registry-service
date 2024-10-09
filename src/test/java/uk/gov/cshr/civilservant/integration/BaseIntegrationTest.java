package uk.gov.cshr.civilservant.integration;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import uk.gov.cshr.civilservant.controller.CSRSControllerTestBase;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class BaseIntegrationTest extends CSRSControllerTestBase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(9000)
            .notifier(new ConsoleNotifier(true)), false);

}
