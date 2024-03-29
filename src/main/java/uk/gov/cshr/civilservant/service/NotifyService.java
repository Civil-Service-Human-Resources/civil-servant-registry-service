package uk.gov.cshr.civilservant.service;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Service
public class NotifyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyService.class);

  @Value("${govNotify.key}")
  private String govNotifyKey;

  @Value("${govNotify.enabled}")
  private Boolean enabled;

  public void notify(String email, String templateId, HashMap<String, String> personalisation) throws NotificationClientException {
    if(enabled){
      NotificationClient client = new NotificationClient(govNotifyKey);
      SendEmailResponse response = client.sendEmail(templateId, email, personalisation, "");
      LOGGER.debug("Line manager notification email: {}", response.getBody());
    }
    else{
      LOGGER.info("Gov Notify disabled - email {}, personalisation {}", email, personalisation.toString());
    }
  }
}
