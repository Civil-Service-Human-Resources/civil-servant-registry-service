package uk.gov.cshr.civilservant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.QuizDataTableDto;
import uk.gov.cshr.civilservant.dto.QuizDto;
import uk.gov.cshr.civilservant.dto.QuizResultSummaryDto;
import uk.gov.cshr.civilservant.dto.SkillsReportsDto;
import uk.gov.cshr.civilservant.dto.factory.QuizDtoFactory;
import uk.gov.cshr.civilservant.exception.ProfessionNotFoundException;
import uk.gov.cshr.civilservant.repository.ProfessionRepository;
import uk.gov.cshr.civilservant.repository.QuizRepository;
import uk.gov.cshr.civilservant.repository.QuizResultRepository;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QuizServiceTest {

  @Mock QuizRepository quizRepository;

  @Mock QuestionService questionService;

  @Mock QuizResultRepository quizResultRepository;

  @Mock QuizDtoFactory quizDtoFactory;

  @Mock ProfessionRepository professionRepository;

  @Mock ObjectMapper objectMapper;

  @InjectMocks QuizService quizService;

  @Test
  public void shouldGetProfessionById() {
    // Given
    Long professionId = 1L;
    Quiz quiz = QuizBuilder.buildEntity();
    QuizDto quizDTO = QuizBuilder.buildQuizDTO().get();
    // When
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.of(quiz));
    when(quizDtoFactory.create(quiz)).thenReturn(quizDTO);

    // then

    QuizDto expected = quizService.getQuizByProfessionId(professionId).get();
    assertTrue(expected.equals(quizDTO));
  }

  @Test
  public void shouldGetAllQuizzesInTheSystem() {
    // Given
    QuizResultSummaryDto quizResultSummaryDto =
        QuizResultSummaryDto.builder()
            .averageScore(23.0)
            .numberOfAttempts(2)
            .professionId(1)
            .build();
    QuizDataTableDto quizDataTableDto =
        QuizDataTableDto.builder().profession("").averageScore(23).numberOfAttempts(2).build();
    // When
    when(quizResultRepository.findAllResults()).thenReturn(Arrays.asList(quizResultSummaryDto));

    // then
    Optional<List<QuizDataTableDto>> expected = quizService.getAllResults();
    assertTrue(expected.get().get(0).getProfession().equals(quizDataTableDto.getProfession()));
    assertTrue(
        expected.get().get(0).getNumberOfAttempts() == quizDataTableDto.getNumberOfAttempts());
    assertTrue(expected.get().get(0).getAverageScore() == quizDataTableDto.getAverageScore());
  }

  @Test
  public void shouldReturnNothingWhenNothingFoundForGetProfessionById() {
    // Given
    Long professionId = 1L;

    // When
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.empty());

    // then

    assertTrue(quizService.getQuizByProfessionId(professionId).equals(Optional.empty()));
  }

  @Test
  public void shouldPersistQuizzesInTheSystem() {
    // Given
    Quiz quiz = QuizBuilder.buildEntity();
    QuizDto quizDTO = QuizBuilder.buildQuizDTO().get();

    // When
    when(quizRepository.save(quiz)).thenReturn(quiz);
    when(quizDtoFactory.create(quiz)).thenReturn(quizDTO);

    // then
    QuizDto expected = quizService.save(quiz);
    assertTrue(expected.equals(quizDTO));
  }

  @Test
  public void shouldDeleteQuizzesInTheSystem() {
    // Given
    Long professionId = 1L;
    Quiz quiz = QuizBuilder.buildEntity();

    // when
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.of(quiz));

    // then
    quizService.delete(professionId);
    verify(quizRepository, times(1)).save(quiz);
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowExceptionIfActiveQuizNotFound() {
    // Given
    Long professionId = 1L;

    // when
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.empty());

    // then
    quizService.delete(professionId);
  }

  @Test
  public void shouldUpdateQuizDescription() {
    // Given
    Long professionId = 1L;
    Quiz quiz = QuizBuilder.buildEntity();
    QuizDto quizDto = QuizBuilder.buildQuizDTO().get();
    quizDto.setDescription("Test update");
    // when
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE))
        .thenReturn(Optional.of(quiz));
    when(quizRepository.save(quiz)).thenReturn(quiz);
    when(quizDtoFactory.create(quiz)).thenReturn(quizDto);

    quiz.setDescription("Test update");
    // then
    QuizDto updatedQuiz = quizService.update(quiz, professionId);
    verify(quizRepository, times(1)).save(quiz);
    assertEquals("Test update", updatedQuiz.getDescription());
  }

  @Test
  public void shouldReturnExistingQuizFromCreateQuiz() throws ProfessionNotFoundException {
    // Given
    Quiz quiz = QuizBuilder.buildEntity();
    QuizDto quizDTO = QuizBuilder.buildQuizDTO().get();

    // When
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(anyLong(), any()))
        .thenReturn(Optional.of(quiz));
    when(quizDtoFactory.create(quiz)).thenReturn(quizDTO);

    // then
    QuizDto expected = quizService.create(1L);
    assertTrue(expected.equals(quizDTO));
  }

  @Test
  public void shouldCreateNewQuizIfNoneFoundForProfession() throws ProfessionNotFoundException {
    // Given
    Quiz quiz = QuizBuilder.buildEntity();
    QuizDto quizDTO = QuizBuilder.buildQuizDTO().get();

    // When
    when(professionRepository.findById(anyLong()))
        .thenReturn(Optional.of(Profession.builder().build()));
    when(quizRepository.findFirstByProfessionIdAndStatusIsNot(anyLong(), any()))
        .thenReturn(Optional.empty());
    when(quizRepository.save(any())).thenReturn(quiz);
    when(quizDtoFactory.create(any())).thenReturn(quizDTO);

    // then
    QuizDto expected = quizService.create(1L);
    assertTrue(expected.equals(quizDTO));
  }

  @Test
  public void shouldReturnReportsForSuperAdmin() throws IOException {
    // Given

    // when
    when(quizResultRepository.findAllByCompletedOnBetween(any(), any()))
        .thenReturn(QuizBuilder.buildSomeResults());
    when(questionService.findAll(anySet())).thenReturn(QuizBuilder.buildSomeQuestions());
    when(objectMapper.readValue(anyString(), eq(Question.class)))
        .thenReturn(Question.builder().id(1L).build());
    // then
    List<SkillsReportsDto> expectedList = quizService.getReportForSuperAdmin(any(), any());

    assertTrue(expectedList.size() > 0);
    assertEquals(1, expectedList.get(1).getQuestionId());
  }

  @Test
  public void shouldReturnReportsForOrgAdmin() throws IOException {
    // Given

    // when
    when(quizResultRepository.findAllByOrganisationIdAndCompletedOnBetween(anyLong(), any(), any()))
        .thenReturn(QuizBuilder.buildSomeResults());
    when(questionService.findAll(anySet())).thenReturn(QuizBuilder.buildSomeQuestions());
    when(objectMapper.readValue(anyString(), eq(Question.class)))
        .thenReturn(Question.builder().id(1L).build());
    // then
    List<SkillsReportsDto> expectedList =
        quizService.getReportForOrganisationAdmin(anyLong(), any(), any());

    assertTrue(expectedList.size() > 0);
    assertEquals(1, expectedList.get(1).getQuestionId());
  }

  @Test
  public void shouldReturnReportsForProfReporter() throws IOException {
    // Given

    // when
    when(quizResultRepository.findAllByOrganisationIdAndProfessionIdAndCompletedOnBetween(
            anyLong(), anyLong(), any(), any()))
        .thenReturn(QuizBuilder.buildSomeResults());
    when(questionService.findAll(anySet())).thenReturn(QuizBuilder.buildSomeQuestions());
    when(objectMapper.readValue(anyString(), eq(Question.class)))
        .thenReturn(Question.builder().id(1L).build());
    // then
    List<SkillsReportsDto> expectedList =
        quizService.getReportForProfessionReporter(anyLong(), anyLong(), any(), any());

    assertTrue(expectedList.size() > 0);
    assertEquals(1, expectedList.get(1).getQuestionId());
  }

  @Test
  public void shouldReturnReportsForProfAdmin() throws IOException {
    // Given

    // when
    when(quizResultRepository.findAllByProfessionIdAndCompletedOnBetween(anyLong(), any(), any()))
        .thenReturn(QuizBuilder.buildSomeResults());
    when(questionService.findAll(anySet())).thenReturn(QuizBuilder.buildSomeQuestions());
    when(objectMapper.readValue(anyString(), eq(Question.class)))
        .thenReturn(Question.builder().id(1L).build());
    // then
    List<SkillsReportsDto> expectedList =
        quizService.getReportForProfessionAdmin(anyLong(), any(), any());

    assertTrue(expectedList.size() > 0);
    assertEquals(1, expectedList.get(1).getQuestionId());
  }

    @Test
    public void deleteQuizResultsCompletedBeforeDate() {
        LocalDateTime now = LocalDateTime.now();

        when(quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(now)).thenReturn(1L);

        long deleteCount = quizService.deleteQuizResultsCompletedBeforeDate(now);
        verify(quizResultRepository, times(1)).deleteQuizResultsByCompletedOnIsLessThanEqual(now);
        assertEquals(1, deleteCount);
        verifyNoMoreInteractions(quizResultRepository);
    }
}
