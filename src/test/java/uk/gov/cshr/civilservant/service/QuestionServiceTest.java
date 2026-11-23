package uk.gov.cshr.civilservant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.QuestionDto;
import uk.gov.cshr.civilservant.dto.factory.AnswerDtoFactory;
import uk.gov.cshr.civilservant.dto.factory.QuestionDtoFactory;
import uk.gov.cshr.civilservant.exception.QuizNotFoundException;
import uk.gov.cshr.civilservant.exception.QuizServiceException;
import uk.gov.cshr.civilservant.repository.AnswerRepository;
import uk.gov.cshr.civilservant.repository.QuestionRepository;
import uk.gov.cshr.civilservant.repository.QuizRepository;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

  @Mock QuestionDtoFactory questionDtoFactory;

  @Mock AnswerDtoFactory answerDtoFactory;

  @Mock AnswerRepository answerRepository;

  @Mock QuestionRepository questionRepository;

  @Mock QuizRepository quizRepository;

  @InjectMocks QuestionService questionService;

  @Test
  public void shouldAddANewQuestionToQuiz() throws QuizServiceException, QuizNotFoundException {
    // Given
    Long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);
    Quiz quiz = QuizBuilder.buildEntity();
    Question entity = QuizBuilder.buildAQuestionEntity();
    Answer answer = QuizBuilder.buildAnAnswer();

    // when
    doReturn(Optional.of(quiz))
        .when(quizRepository)
        .findFirstByProfessionIdAndStatusIsNot(anyLong(), any());
    when(questionDtoFactory.createEntity(any())).thenReturn(entity);
    when(answerDtoFactory.createEntity(any())).thenReturn(answer);
    when(questionRepository.save(any())).thenReturn(entity);

    // then
    Long expectedId = questionService.addQuizQuestion(professionId, questionDto);
    assertTrue(expectedId.equals(1L));
  }

  @Test
  public void shouldUpdateAQuestionInTheQuiz() throws QuizServiceException, QuizNotFoundException {
    // Given
    Long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(professionId);
    questionDto.setType(QuestionType.SINGLE);

    Question entity = QuizBuilder.buildAQuestionEntity();
    Answer answer = QuizBuilder.buildAnAnswer();

    // when
    when(questionDtoFactory.createEntity(questionDto)).thenReturn(entity);
    when(answerDtoFactory.createEntity(any())).thenReturn(answer);
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.of(entity));
    when(questionRepository.save(any())).thenReturn(entity);
    when(answerRepository.findById(questionDto.getAnswer().getId()))
        .thenReturn(Optional.of(answer));

    // then
    Long expectedId = questionService.updateQuizQuestion(questionDto);
    assertTrue(expectedId.equals(1L));
  }

  @Test
  public void shouldDeleteQuestions() {
    // Given
    Long questionId = 1L;
    Question question =
        Question.builder().id(1L).quiz(Quiz.builder().numberOfQuestions(4).build()).build();

    // when
    when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

    // then
    questionService.deleteQuestion(questionId);
    assertEquals(question.getStatus(), Status.INACTIVE);
    assertEquals(3, (int) question.getQuiz().getNumberOfQuestions());
    verify(questionRepository, times(1)).save(question);
  }

  @Test
  public void shouldThrowQuizNotFoundExceptionWhenUpdatingAQuestionInAnInactiveQuiz()
      throws QuizServiceException, QuizNotFoundException {
    // Given
    Long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(professionId);
    questionDto.setType(QuestionType.SINGLE);

    Question entity = QuizBuilder.buildAQuestionEntity();
    entity.getQuiz().setStatus(Status.INACTIVE);

    // when
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.of(entity));

    // then
    assertThrows(
            QuizNotFoundException.class,
            () -> questionService.updateQuizQuestion(questionDto)
    );
    verify(questionRepository, times(1)).findById(questionDto.getId());
  }

  @Test
  public void shouldThrowQuizNotFoundExceptionWhenUpdatingAnInactiveQuestionInQuiz(){
    // Given
    Long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(professionId);
    questionDto.setType(QuestionType.SINGLE);

    Question entity = QuizBuilder.buildAQuestionEntity();
    entity.setStatus(Status.INACTIVE);

    // when
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.of(entity));

    // then
    assertThrows(
            QuizServiceException.class,
            () -> questionService.updateQuizQuestion(questionDto)
    );

    verify(questionRepository, times(1)).findById(questionDto.getId());
  }

  @Test
  public void shouldNotAddANewQuestionToAnInactiveQuiz()
      throws QuizServiceException, QuizNotFoundException {
    // Given
    long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);

    // when
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.empty());

    // then
    assertThrows(
            QuizNotFoundException.class,
            () -> questionService.addQuizQuestion(professionId, questionDto)
    );
  }

  @Test
  public void shouldThrowExceptionOnUpdateIfQuestionNotFound()
      throws QuizServiceException, QuizNotFoundException {
    // Given
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);

    // when
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.empty());

    // then
    assertThrows(
            QuizServiceException.class,
            () -> questionService.updateQuizQuestion(questionDto)
    );
  }

  @Test
  public void shouldNotAddANewQuestionIfAltTextNotFoundForQuestionWithImageUrl(){
    // Given
    long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);

    // when
    questionDto.setImgUrl("www.gov.uk");

    // then
    assertThrows(
            QuizServiceException.class,
            () -> questionService.addQuizQuestion(professionId, questionDto)
    );
  }

  @Test
  public void shouldUpdateAQuestionWithMultipleAnswersInTheQuiz()
      throws QuizServiceException, QuizNotFoundException {
    // Given
    Long professionId = 1L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(professionId);
    questionDto.setType(QuestionType.SINGLE);
    questionDto.getAnswer().setCorrectAnswers(new String[] {"A", "B"});

    Question entity = QuizBuilder.buildAQuestionEntity();
    Answer answerEntity = QuizBuilder.buildAnAnswer();

    // when
    when(questionDtoFactory.createEntity(questionDto)).thenReturn(entity);
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.of(entity));
    when(questionRepository.save(any())).thenReturn(entity);
    when(answerRepository.findById(anyLong())).thenReturn(Optional.of(answerEntity));
    when(answerDtoFactory.createEntity(any())).thenReturn(answerEntity);

    // then
    Long expectedId = questionService.updateQuizQuestion(questionDto);
    assertTrue(expectedId.equals(1L));
  }

  @Test
  public void shouldReturnQuestionIfValidIdProvided() {
    // Given
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);
    Question entity = QuizBuilder.buildAQuestionEntity();
    // when
    when(questionRepository.findById(questionDto.getId())).thenReturn(Optional.of(entity));

    when(questionDtoFactory.create(entity)).thenReturn(questionDto);

    // then
    QuestionDto expected = questionService.getById(1L).get();

    assertEquals(expected, questionDto);
  }

  @Test
  public void shouldThrowExceptionOnDeletingNonExistingQuestion() {
    // Given
    long questionId = 1L;
    // when
    when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

    // then
    assertThrows(
            EntityNotFoundException.class,
            () -> questionService.deleteQuestion(questionId)
    );
    verify(questionRepository, times(1)).findById(questionId);
  }
}
