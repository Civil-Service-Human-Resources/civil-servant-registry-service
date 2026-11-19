package uk.gov.cshr.civilservant.service;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.Answer;
import uk.gov.cshr.civilservant.repository.AnswerRepository;

@Slf4j
@Service
@Transactional
public class AnswerService {
  AnswerRepository answerRepository;

  @Autowired
  public AnswerService(AnswerRepository answerRepository) {
    this.answerRepository = answerRepository;
  }

  public Optional<Answer> getByQuestion(Long questionId) {
    return answerRepository.findByQuestion_Id(questionId);
  }
}
