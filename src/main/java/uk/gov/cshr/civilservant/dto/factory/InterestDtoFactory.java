package uk.gov.cshr.civilservant.dto.factory;

import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.Interest;
import uk.gov.cshr.civilservant.dto.InterestDto;

@Component
public class InterestDtoFactory extends DtoFactory<InterestDto, Interest> {

  public InterestDto createSimple(Interest interest) {
    InterestDto interestDto = new InterestDto();
    interestDto.setId(interest.getId());
    interestDto.setName(interest.getName());
    return interestDto;
  }

  @Override
  public InterestDto create(Interest interest) {
    return createSimple(interest);
  }
}
