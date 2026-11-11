package uk.gov.cshr.civilservant.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.Answer;

@Repository
@PreAuthorize("isAuthenticated()")
public interface AnswerRepository extends CrudRepository<Answer, Long> {
  Optional<Answer> findByQuestion_Id(Long questionId);
}
