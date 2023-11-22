package uk.gov.cshr.civilservant.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.domain.AgencyDomain;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AgencyTokenRepositoryTest {

    private static final String UID = "uid";

    @Autowired
    private OrganisationalUnitRepository organisationalUnitRepository;

    @Autowired
    private AgencyTokenRepository agencyTokenRepository;

    private AgencyToken agencyToken = null;

    @Before
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
        assertThat(actual.get().getToken(), equalTo("token4Test"));
        assertThat(actual.get().getCapacity(), equalTo(100));
        assertThat(actual.get().getId(), is(notNullValue()));
        assertThat(actual.get().getAgencyDomains(), is(nullValue()));
    }

    @Test
    public void shouldFindAgencyTokenIncludingAgencyDomainsByExistingDomainAndTokenAndCode() {

        Optional<AgencyToken> actual = agencyTokenRepository.findByDomainTokenAndCodeIncludingAgencyDomains("nhs.scot", "token4Test", "nhs");

        assertTrue(actual.isPresent());
        assertThat(actual.get().getToken(), equalTo("token4Test"));
        assertThat(actual.get().getCapacity(), equalTo(100));
        assertThat(actual.get().getUid(), equalTo(UID));
        assertThat(actual.get().getId(), is(notNullValue()));
        assertThat(actual.get().getAgencyDomains(), hasSize(3));
    }
}
