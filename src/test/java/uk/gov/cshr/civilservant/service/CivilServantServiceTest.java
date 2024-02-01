package uk.gov.cshr.civilservant.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CivilServantServiceTest {

    @Mock
    private CivilServantRepository civilServantRepository;

    @InjectMocks
    private CivilServantService classUnderTest;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldPerformCsrsLoginCheckAndDeleteOrganisation() {
        IdentityDTO dto = new IdentityDTO("1", "CS@cs.gov.uk", Collections.singleton("ROLE"));
        OrganisationalUnit ou = new OrganisationalUnit();
        Domain domain = new Domain("civil-service.gov.uk");
        ou.addDomain(domain);
        Identity identity = new Identity("1");
        CivilServant cs = new CivilServant(identity);
        cs.setOrganisationalUnit(ou);
        when(civilServantRepository.findByPrincipal()).thenReturn(Optional.of(cs));
        Optional<CivilServant> resultOpt = classUnderTest.performLogin(dto);
        assertFalse(resultOpt.get().getOrganisationalUnit().isPresent());
    }

    @Test
    public void shouldReturnCivilServantUid() {
        Identity identity = new Identity("1");
        CivilServant cs = new CivilServant(identity);
        Optional<CivilServant> optionalCivilServant = Optional.of(cs);
        when(civilServantRepository.findByPrincipal()).thenReturn(optionalCivilServant);

        String actual = classUnderTest.getCivilServantUid();

        assertThat(actual, equalTo("1"));
    }

    @Test
    public void shouldThrowCivilServantNotFoundExceptionWhenCivilServantHasNoIdentity() {
        CivilServant cs = new CivilServant();
        Optional<CivilServant> optionalCivilServant = Optional.of(cs);
        when(civilServantRepository.findByPrincipal()).thenReturn(optionalCivilServant);

        expectedException.expect(CivilServantNotFoundException.class);

        String actual = classUnderTest.getCivilServantUid();

        assertThat(actual, isNull());
    }

    @Test
    public void shouldThrowCivilServantNotFoundExceptionWhenCivilServantNotFound() {
        when(civilServantRepository.findByPrincipal()).thenReturn(Optional.empty());

        expectedException.expect(CivilServantNotFoundException.class);

        String actual = classUnderTest.getCivilServantUid();

        assertThat(actual, isNull());
    }

    @Test
    public void shouldThrowActualExceptionWhenTechnicalError() {
        RuntimeException runtimeException = new RuntimeException("broken");
        when(civilServantRepository.findByPrincipal()).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("broken");

        String actual = classUnderTest.getCivilServantUid();

        assertThat(actual, isNull());
    }

}
