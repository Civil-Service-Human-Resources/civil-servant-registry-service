package uk.gov.cshr.civilservant.dto.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.dto.AgencyDomainDTO;
import uk.gov.cshr.civilservant.dto.AgencyTokenDTO;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
public class AgencyTokenFactoryTest {

    private static final String TOKEN = "TOKEN123";
    private static final int CAPACITY = 100;
    private static final String DOMAIN = "example.com";
    private static final String UID = "UID";

    @InjectMocks
    private AgencyTokenFactory agencyTokenFactory;

    @Test
    public void buildAgencyTokenShouldBuildAndSetNewUid() {
        AgencyTokenDTO AgencyTokenDto = new AgencyTokenDTO();
        AgencyTokenDto.setCapacity(CAPACITY);
        AgencyTokenDto.setToken(TOKEN);

        AgencyDomainDTO agencyDomainDTO = new AgencyDomainDTO(DOMAIN);
        Set<AgencyDomainDTO> agencyDomainDTOS = new HashSet<>();
        agencyDomainDTOS.add(agencyDomainDTO);

        AgencyTokenDto.setAgencyDomains(agencyDomainDTOS);

        AgencyToken agencyToken = agencyTokenFactory.buildAgencyTokenFromAgencyTokenDto(AgencyTokenDto);

        assertNotEquals(agencyToken.getUid(), UID);
        assertEquals(agencyToken.getCapacity(), CAPACITY);
        assertEquals(agencyToken.getToken(), TOKEN);
    }

    @Test
    public void buildAgencyTokenShouldBuildAndUseExistingUid() {
        AgencyTokenDTO AgencyTokenDto = new AgencyTokenDTO();
        AgencyTokenDto.setCapacity(CAPACITY);
        AgencyTokenDto.setToken(TOKEN);
        AgencyTokenDto.setUid(UID);

        AgencyDomainDTO agencyDomainDTO = new AgencyDomainDTO(DOMAIN);
        Set<AgencyDomainDTO> agencyDomainDTOS = new HashSet<>();
        agencyDomainDTOS.add(agencyDomainDTO);

        AgencyTokenDto.setAgencyDomains(agencyDomainDTOS);

        AgencyToken agencyToken = agencyTokenFactory.buildAgencyTokenFromAgencyTokenDto(AgencyTokenDto);

        assertEquals(agencyToken.getUid(), UID);
        assertEquals(agencyToken.getCapacity(), CAPACITY);
        assertEquals(agencyToken.getToken(), TOKEN);
    }
}
