package uk.gov.cshr.civilservant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.civilservant.domain.Quiz;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.QuizDtoFactory;
import uk.gov.cshr.civilservant.repository.QuizRepository;
import uk.gov.cshr.civilservant.service.QuizBuilder;
import uk.gov.cshr.civilservant.service.QuizService;

import java.io.File;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class QuizControllerTest extends CSRSControllerTestBase {
  @Autowired ObjectMapper objectMapper;
  @MockBean QuizDtoFactory quizDTOFactory;
  @MockBean QuizRepository quizRepository;
  @MockBean QuizService quizService;

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNER"})
  public void shouldGetQuizForProfession() throws Exception {
    // Given
    Optional<QuizDto> quiz = QuizBuilder.buildQuizDTO();
    long professionId = 1L;

    // when
    when(quizService.getQuizByProfessionId(professionId)).thenReturn(quiz);

    // then

    mockMvc
        .perform(
            get("/api/quiz")
                .param("professionId", "1")
                .param("organisationId", "1")
                .param("limit", "3")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(result -> equalTo(quiz));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNING_MANAGER", "CSHR_REPORTER"})
  public void shouldCreateQuizForProfession() throws Exception {
    // Given
    QuizDto quiz = QuizBuilder.buildQuizDTO().get();
    String quizJson = objectMapper.writeValueAsString(quiz);

    // when
    Quiz quizRecord = QuizBuilder.buildEntity();
    when(quizDTOFactory.mapDtoToModel(any())).thenReturn(quizRecord);
    quiz.setId(1L);
    when(quizService.create(anyLong())).thenReturn(quiz);

    // then

    mockMvc
        .perform(
            post("/api/quiz")
                .with(csrf())
                .content(quizJson)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quiz)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {
        "LEARNING_MANAGER",
        "CSHR_REPORTER",
        "ORGANISATION_REPORTER",
        "PROFESSION_REPORTER"
      })
  public void shouldReturnNothingIfNoQuizFoundForProfession() throws Exception {
    // Given a profession id
    Long professionId = 1L;

    // when
    when(quizService.getQuizByProfessionId(professionId)).thenReturn(Optional.empty());

    // then

    mockMvc
        .perform(get("/api/quiz/1").with(csrf()).accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {
        "LEARNING_MANAGER",
        "CSHR_REPORTER",
        "ORGANISATION_REPORTER",
        "PROFESSION_REPORTER"
      })
  public void shouldUpdateQuiz() throws Exception {
    // Given
    QuizDto quiz = QuizBuilder.buildQuizDTO().get();
    Quiz quizRecordToBeUpdated = QuizBuilder.buildEntity();
    Quiz quizFromDto = QuizBuilder.buildEntity();
    quizFromDto.setDescription("Some test description");
    String quizJson = objectMapper.writeValueAsString(quiz);
    System.out.println(quizJson);

    // when
    when(quizDTOFactory.mapDtoToModel(any())).thenReturn(quizFromDto);
    when(quizRepository.findById(quizFromDto.getId()))
        .thenReturn(Optional.of(quizRecordToBeUpdated));
    when(quizService.update(any(), anyLong())).thenReturn(quiz);

    // then

    mockMvc
        .perform(
            post("/api/quiz/update")
                .with(csrf())
                .content(quizJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quiz)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {
        "LEARNING_MANAGER",
        "CSHR_REPORTER",
        "ORGANISATION_REPORTER",
        "PROFESSION_REPORTER"
      })
  public void shouldDeleteQuizForProfession() throws Exception {
    // Given
    QuizDto quiz = QuizBuilder.buildQuizDTO().get();
    String quizJson = objectMapper.writeValueAsString(quiz);
    System.out.println(quizJson);

    // when
    Quiz quizRecord = QuizBuilder.buildEntity();
    when(quizDTOFactory.mapDtoToModel(any())).thenReturn(quizRecord);
    when(quizService.save(any())).thenReturn(quiz);

    // then

    mockMvc
        .perform(
            delete("/api/quiz/delete")
                .param("professionId", "1")
                .param("organisationId", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNING_MANAGER", "CSHR_REPORTER"})
  public void shouldGetAllQuizResultsForProfession() throws Exception {
    // Given a profession id
    Long professionId = 1L;
    File file = new File("src/test/resources/test_data/results_by_profession.json");
    QuizDataTableDto quizResult = objectMapper.readValue(file, QuizDataTableDto.class);

    // when
    when(quizService.getAllResultsForProfession(professionId))
        .thenReturn(Optional.of(quizResult));

    // then

    mockMvc
        .perform(
            get("/api/quiz/results-by-profession")
                .param("professionId", "1")
                .param("organisationId", "1")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResult)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNER"})
  public void shouldGetHistoryForStaff() throws Exception {
    // Given a profession id
    String staffId = "1232";
    File file = new File("src/test/resources/test_data/quiz_history_for_staff.json");
    QuizHistoryDto quizHistoryDto = objectMapper.readValue(file, QuizHistoryDto.class);

    // when
    when(quizService.getQuizHistory(staffId)).thenReturn(Optional.of(quizHistoryDto));

    // then

    mockMvc
        .perform(
            get("/api/quiz/quiz-history?staffId=1232")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(content().json(objectMapper.readTree(file).toString()));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNER"})
  public void shouldGetQuizSummary() throws Exception {
    // Given
    String quizResultId = "1";
    String staffId = "1232";

    File file = new File("src/test/resources/test_data/quiz_summary.json");
    QuizResultDto quizResultDto = objectMapper.readValue(file, QuizResultDto.class);
    // when
    when(quizService.getQuizResult(anyLong(), any())).thenReturn(quizResultDto);

    // then

    mockMvc
        .perform(
            get("/api/quiz/quiz-summary")
                .with(csrf())
                .param("staffId", staffId)
                .param("quizResultId", quizResultId)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.readTree(file).toString()));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {"LEARNER"})
  public void shouldSubmitAnswersToQuiz() throws Exception {
    // Given
    Long resultId = 1L;
    File file = new File("src/test/resources/test_data/submitted_answers.json");

    QuizSubmissionDto quizSubmissionDto = objectMapper.readValue(file, QuizSubmissionDto.class);

    // when
    when(quizService.submitAnswers(any())).thenReturn(Optional.of(resultId));

    // then

    mockMvc
        .perform(
            post("/api/quiz/submit-answers")
                .with(csrf())
                .content(objectMapper.writeValueAsString(quizSubmissionDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(1)));
  }

  @Test
  @WithMockUser(
      username = "user",
      authorities = {
        "LEARNING_MANAGER",
        "CSHR_REPORTER",
        "ORGANISATION_REPORTER",
        "PROFESSION_REPORTER"
      })
  public void shouldPublishQuiz() throws Exception {
    // Given
    QuizDto quiz = QuizBuilder.buildQuizDTO().get();
    String quizJson = objectMapper.writeValueAsString(quiz);
    System.out.println(quizJson);

    // when
    when(quizService.publish(any())).thenReturn(1L);

    // then

    mockMvc
        .perform(
            put("/api/quiz/publish")
                .with(csrf())
                .content(quizJson)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("1"));
  }
}
