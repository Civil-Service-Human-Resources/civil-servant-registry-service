package uk.gov.cshr.civilservant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.civilservant.domain.Profession;
import uk.gov.cshr.civilservant.repository.ProfessionRepository;

@ExtendWith(MockitoExtension.class)
public class ProfessionServiceTest {
  @Mock private ProfessionRepository professionRepository;

  @InjectMocks private ProfessionService professionService;

  @Test
  public void shouldReturnListOfParents() {
    Profession parent1 = new Profession();
    parent1.setName("parent1");
    Profession child1 = new Profession();
    child1.setName("child1");
    Profession child2 = new Profession();
    child2.setName("child2");
    child1.setParent(parent1);
    child2.setParent(child1);

    Profession parent2 = new Profession();
    parent2.setName("parent2");

    when(professionRepository.findAllByOrderByNameAsc())
        .thenReturn(Arrays.asList(parent1, child1, child2, parent2));

    List<Profession> result = professionService.getParents();

    assertEquals(Arrays.asList(parent1, parent2), result);
  }
}
