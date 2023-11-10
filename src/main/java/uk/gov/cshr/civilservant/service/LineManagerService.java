package uk.gov.cshr.civilservant.service;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.service.identity.IdentityFromService;
import uk.gov.cshr.civilservant.service.identity.IdentityService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;

@Service
public class LineManagerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyService.class);

  private NotifyService notifyService;

  private IdentityService identityService;

  @Value("${govNotify.template.lineManager}")
  private String govNotifyLineManagerTemplateId;

  @Autowired
  public LineManagerService(IdentityService identityService, NotifyService notifyService) {
    this.identityService = identityService;
    this.notifyService = notifyService;
  }

  public IdentityFromService checkLineManager(String email) {
    return identityService.findByEmail(email);
  }

  public void notifyLineManager(CivilServant civilServant, CivilServant lineManager, String email) {

    String learnerName = defaultIfNull(civilServant.getFullName(), "");
    String lineManagerName = defaultIfNull(lineManager.getFullName(), "");

    try {
      HashMap<String, String> personalisation = getLineManagerNotificationPersonalisation(lineManagerName, learnerName, identityService.getEmailAddress(civilServant));
      notifyService.notify(email, govNotifyLineManagerTemplateId, personalisation);
    } catch (NotificationClientException nce) {
      LOGGER.error("Could not send Line Manager notification", nce);
    }
  }

  private HashMap<String, String> getLineManagerNotificationPersonalisation(String lineManagerName, String learnerName, String learnerEmail){
    HashMap<String, String> personalisation = new HashMap<>();
    personalisation.put("lineManagerName", lineManagerName);
    personalisation.put("learnerName", learnerName);
    personalisation.put("learnerEmailAddress", learnerEmail);

    return personalisation;
  }
}
