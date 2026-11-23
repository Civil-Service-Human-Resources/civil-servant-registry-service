package uk.gov.cshr.civilservant.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.gov.cshr.civilservant.domain.AgencyDomain;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class AgencyTokenRepositoryTest {

    private static final String UID = "uid";

    @Autowired
    private OrganisationalUnitRepository organisationalUnitRepository;

    @Autowired
    private AgencyTokenRepository agencyTokenRepository;

    private AgencyToken agencyToken = null;

    @BeforeEach
    public void setUp() {
        agencyToken = new AgencyToken();
        agencyToken.setToken("token4Test");
        agencyToken.setCapacity(100);
        agencyToken.setUid(UID);

        AgencyDomain domainNHSDigital = new AgencyDomain();
        domainNHSDigital.setDomain("nhs.digital");

        AgencyDomain domainNHSGovUk = new AgencyDomain();
        domainNHSGovUk.setDomain("nhs.gov.uk");

        AgencyDomain domainNHSScotland = new AgencyDomain();
        domainNHSScotland.setDomain("nhs.scot");

        Set<AgencyDomain> agencyDomains = new HashSet<>();
        agencyDomains.add(domainNHSDigital);
        agencyDomains.add(domainNHSGovUk);
        agencyDomains.add(domainNHSScotland);

        agencyToken.setAgencyDomains(agencyDomains);

        OrganisationalUnit ou = new OrganisationalUnit();
        ou.setCode("nhs");
        ou.setAgencyToken(agencyToken);
        ou.setName("nhs org for test");
        ou.setAbbreviation("NHS");
        organisationalUnitRepository.saveAndFlush(ou);
    }

    @Test
    public void shouldFindAgencyTokenByExistingDomainAndTokenAndCode() {

        Optional<AgencyToken> actual = agencyTokenRepository.findByDomainTokenAndCode("nhs.scot", "token4Test", "nhs");

        assertTrue(actual.isPresent());
        assertEquals(actual.get().getToken(), "token4Test");
        assertEquals(actual.get().getCapacity(), 100);
        assertNotNull(actual.get().getId());
        assertNull(actual.get().getAgencyDomains());
    }

    @Test
    public void shouldFindAgencyTokenIncludingAgencyDomainsByExistingDomainAndTokenAndCode() {

        Optional<AgencyToken> actual = agencyTokenRepository.findByDomainTokenAndCodeIncludingAgencyDomains("nhs.scot", "token4Test", "nhs");

        assertTrue(actual.isPresent());
        assertEquals(actual.get().getToken(), "token4Test");
        assertEquals(actual.get().getCapacity(), 100);
        assertEquals(actual.get().getUid(), UID);
        assertNotNull(actual.get().getUid());
        assertEquals(actual.get().getAgencyDomains().size(), 3);
    }
}
