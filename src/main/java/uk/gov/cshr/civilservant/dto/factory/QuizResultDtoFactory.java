package uk.gov.cshr.civilservant.dto.factory;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.Question;
import uk.gov.cshr.civilservant.domain.QuizResult;
import uk.gov.cshr.civilservant.domain.SubmittedAnswer;
import uk.gov.cshr.civilservant.dto.QuizResultDto;
import uk.gov.cshr.civilservant.dto.QuizResultQuestionDto;
import uk.gov.cshr.civilservant.dto.SubmittedAnswerDto;

@Component
public class QuizResultDtoFactory extends DtoFactory<QuizResultDto, QuizResult> {

  ModelMapper modelMapper;
  QuestionDtoFactory questionDtoFactory;
  ObjectMapper objectMapper;

  @Autowired
  public QuizResultDtoFactory(
      ModelMapper modelMapper, QuestionDtoFactory questionDtoFactory, ObjectMapper objectMapper) {
    this.modelMapper = modelMapper;
    this.questionDtoFactory = questionDtoFactory;
    this.objectMapper = objectMapper;
  }

  @Override
  public QuizResultDto create(QuizResult entity) {
    QuizResultDto quizResultDto = modelMapper.map(entity, QuizResultDto.class);
    List<SubmittedAnswer> submittedAnswerRecords = entity.getAnswers();
    List<SubmittedAnswerDto> submittedAnswers = new ArrayList<>();
    submittedAnswerRecords.forEach(
        submittedAnswer -> {
          QuizResultQuestionDto quizResultQuestionDto = null;
          try {
            quizResultQuestionDto =
                modelMapper.map(
                    questionDtoFactory.create(
                        objectMapper.readValue(submittedAnswer.getQuestion(), Question.class)),
                    QuizResultQuestionDto.class);
          } catch (Exception e) {
            e.printStackTrace();
          }
          submittedAnswers.add(
              new SubmittedAnswerDto(
                  (int) submittedAnswer.getId(),
                  submittedAnswer.getSubmittedAnswers(),
                  submittedAnswer.isSkipped(),
                  quizResultQuestionDto));
        });
    quizResultDto.setAnswers(submittedAnswers);
    return quizResultDto;
  }
}
