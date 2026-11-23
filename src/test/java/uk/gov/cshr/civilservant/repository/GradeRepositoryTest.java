package uk.gov.cshr.civilservant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.cshr.civilservant.domain.Grade;


@SpringBootTest
public class GradeRepositoryTest {

  @Autowired private GradeRepository gradeRepository;

  @Test
  public void shouldFindGradesWithNoOrganisation() {
    Iterable<Grade> results = gradeRepository.findByOrganisationalUnitIsNull();
    assertEquals(Iterables.size(results), 11);
  }
}
