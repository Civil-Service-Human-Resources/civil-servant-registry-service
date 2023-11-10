package uk.gov.cshr.civilservant.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityFromService;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class LineManagerServiceTest {

  @InjectMocks private LineManagerService lineManagerService;

  @Mock private NotifyService notifyService;

  @Mock private IdentityService identityService;

  @Mock private CivilServantRepository civilServantRepository;

  @Test
  public void shouldFindExistingIdentity() {
    when(identityService.findByEmail("learner@domain.com")).thenReturn(new IdentityFromService());
    assertNotNull(lineManagerService.checkLineManager("learner@domain.com"));
  }

  @Test
  public void shouldNotFindNonExistantIdentity() {
    assertNull(lineManagerService.checkLineManager("shouldReturnUriStringFromOrg@doesnotexist"));
  }

  @Test
  public void testLineManagerServiceShouldNotifyTheLineManagerWhenNotifyLineManagerIsCalled() throws Exception {
    String learnerName = "Learner Name";
    String learnerEmail = "learner.name@domain.com";
    String lineManagerName = "Linemanager Name";
    String lineManagerEmail = "linemanager.name@domain.com";

    final Identity learnerIdentity = new Identity("uid");
    final CivilServant learner = new CivilServant(learnerIdentity);
    learner.setFullName(learnerName);

    when(identityService.getEmailAddress(learner)).thenReturn(learnerEmail);

    final Identity managerIdentity = new Identity("mid");
    final CivilServant manager = new CivilServant(managerIdentity);
    manager.setFullName(lineManagerName);

    final IdentityFromService lineManager = new IdentityFromService();
    lineManager.setUid(managerIdentity.getUid());
    lineManager.setUsername(lineManagerEmail);

    HashMap<String, String> expectedPersonalisation = new HashMap<>();
    expectedPersonalisation.put("lineManagerName", lineManagerName);
    expectedPersonalisation.put("learnerName", learnerName);
    expectedPersonalisation.put("learnerEmailAddress", learnerEmail);

    lineManagerService.notifyLineManager(learner, manager, lineManagerEmail);

    verify(notifyService).notify(lineManagerEmail, null, expectedPersonalisation);
  }

}
