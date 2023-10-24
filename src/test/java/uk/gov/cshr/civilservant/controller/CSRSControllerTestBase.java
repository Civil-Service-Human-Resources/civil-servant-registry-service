package uk.gov.cshr.civilservant.controller;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.civilservant.utils.MockMVCFilterOverrider;

@AutoConfigureMockMvc
@SpringBootTest
public class CSRSControllerTestBase {
    @Autowired
    protected MockMvc mockMvc;

    @Before
    public void overridePatternMappingFilterProxyFilter() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");
    }
}
