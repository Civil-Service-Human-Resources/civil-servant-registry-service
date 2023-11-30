package uk.gov.cshr.civilservant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.dto.AddQuestionDto;
import uk.gov.cshr.civilservant.dto.QuestionDto;
import uk.gov.cshr.civilservant.dto.factory.QuestionDtoFactory;
import uk.gov.cshr.civilservant.exception.QuizServiceException;
import uk.gov.cshr.civilservant.service.QuestionService;
import uk.gov.cshr.civilservant.service.QuizBuilder;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WithMockUser(
    username = "user",
    authorities = {
      "LEARNING_MANAGER",
      "CSHR_REPORTER",
      "ORGANISATION_REPORTER",
      "PROFESSION_REPORTER"
    })
public class QuestionControllerTest extends CSRSControllerTestBase {
  @Autowired ObjectMapper objectMapper;
  @MockBean QuestionDtoFactory questionDtoFactory;
  @MockBean QuestionService questionService;

  @Test
  public void shouldAddANewQuestionToQuiz() throws Exception {
    // Given

    Integer professionId = 12312312;
    Integer organisationId = 12312312;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);
    String questionsJson =
        objectMapper.writeValueAsString(
            AddQuestionDto.builder()
                .question(questionDto)
                .professionId(professionId)
                .build());
    // then

    mockMvc
        .perform(
            post("/api/questions/add-question")
                .with(csrf())
                .content(questionsJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(result -> equalTo(1L));
  }

  @Test
  public void shouldUpdateQuizWithNewQuestions() throws Exception {
    // Given

    Long quizId = 12312312L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(quizId);
    String questionsJson = objectMapper.writeValueAsString(questionDto);
    // then

    mockMvc
        .perform(
            post("/api/questions/update")
                .with(csrf())
                .content(questionsJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(result -> equalTo(1L));
  }

  @Test
  public void shouldDeleteQuestions() throws Exception {
    // Given

    Long questionId = 12312312L;

    // then

    mockMvc
        .perform(
            delete(String.format("/api/questions/%d/delete", questionId))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  public void shouldThrowServerErrorUpdatingQuestionInQuiz() throws Exception {
    // Given

    Long quizId = 12312312L;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(quizId);
    String questionsJson = objectMapper.writeValueAsString(questionDto);

    // when

    doThrow(QuizServiceException.class).when(questionService).updateQuizQuestion(any());

    // then

    mockMvc
        .perform(
            post("/api/questions/update")
                .with(csrf())
                .content(questionsJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void shouldThrow400WhenAddingANewQuestionToQuiz() throws Exception {
    // Given
    Integer professionId = 12312312;
    Integer organisationId = 12312312;
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);
    String questionsJson =
        objectMapper.writeValueAsString(
            AddQuestionDto.builder()
                .question(questionDto)
                .professionId(professionId)
                .build());
    // when

    doThrow(QuizServiceException.class)
        .when(questionService)
        .addQuizQuestion(anyLong(), any());

    // then

    mockMvc
        .perform(
            post("/api/questions/add-question")
                .with(csrf())
                .content(questionsJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldThrowBadRequestWhenAddingANewQuestionToQuiz() throws Exception {
    // Given

    // when

    doThrow(QuizServiceException.class)
        .when(questionService)
        .addQuizQuestion(anyLong(),any());

    // then

    mockMvc
        .perform(
            post("/api/questions/add-question")
                .with(csrf())
                .content("")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldThrowServerErrorWhenDeletingAQuestionFromQuiz() throws Exception {
    // Given

    // when

    doThrow(EntityNotFoundException.class).when(questionService).deleteQuestion(anyLong());

    // then

    mockMvc
        .perform(delete("/api/questions/1/delete").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void shouldGetQuestionForPreview() throws Exception {
    // Given
    QuestionDto questionDto = QuizBuilder.buildAQuestion(1L);

    // when
    when(questionService.getById(anyLong())).thenReturn(Optional.of(questionDto));

    // then

    mockMvc
        .perform(get("/api/questions/1/preview").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value", is(questionDto.getValue())));
  }
}
