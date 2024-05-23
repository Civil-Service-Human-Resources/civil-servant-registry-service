package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.civilservant.domain.Quiz;
import uk.gov.cshr.civilservant.domain.Roles;
import uk.gov.cshr.civilservant.domain.Status;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.QuizDtoFactory;
import uk.gov.cshr.civilservant.exception.QuizServiceException;
import uk.gov.cshr.civilservant.mapping.RoleMapping;
import uk.gov.cshr.civilservant.service.QuizService;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping(QuizController.QUIZ_ROOT)
public class QuizController {

  static final String QUIZ_ROOT = "/api/quiz";
  private static final String ERROR_MESSAGE =
      "There was a problem while creating or retreiving the quiz."
          + "Check the values provided or please try again later";

  private final QuizService quizService;
  private final QuizDtoFactory quizDTOFactory;

  @Autowired
  public QuizController(QuizService quizService, QuizDtoFactory quizDtoFactory) {
    this.quizService = quizService;
    this.quizDTOFactory = quizDtoFactory;
  }

  @GetMapping
  @RoleMapping({
    Roles.LEARNER,
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity<List<QuestionDto>> listQuestionsByProfession(
      @RequestParam Long professionId, @RequestParam(defaultValue = "18") Integer limit) {
    if (limit < 1) {
      return ResponseEntity.badRequest().build();
    }
    return quizService
        .getQuizByProfessionId(professionId)
        .map(
            quiz -> {
              List<QuestionDto> questions =
                  quiz.getQuestions()
                      .stream()
                      .filter(questionDto -> !questionDto.getStatus().equals(Status.INACTIVE))
                      .collect(Collectors.toList());
              Collections.shuffle(questions);
              if (questions.size() > limit) {
                return ok(questions.stream().limit(limit).collect(Collectors.toList()));
              }
              return ok(questions);
            })
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping
  @Transactional
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity create(@Valid @RequestBody CreateQuizDto createQuizDto) {

    try {
      ProfessionDto profession = createQuizDto.getProfession();
      return ok(quizService.create(profession.getId()));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE);
    }
  }

  @GetMapping("/all-results")
  @RoleMapping({Roles.LEARNING_MANAGER, Roles.CSHR_REPORTER})
  public ResponseEntity<List<QuizDataTableDto>> getAllQuizzesInTheSystem() {
    return quizService
        .getAllResults()
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @DeleteMapping("/delete")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity deleteQuiz(@RequestParam Long professionId) {
    try {
      log.info("Deleting Quiz for profession {}", professionId);
      quizService.delete(professionId);
    } catch (EntityNotFoundException e) {
      log.error("Error in deleting Quiz");
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("There was a problem deleting the quiz :" + e.getMessage());
    }
    return ok().build();
  }

  @PostMapping("/update")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity update(@Valid @RequestBody QuizDto quiz) {
    try {
      Quiz quizRecord = quizDTOFactory.mapDtoToModel(quiz);
      return ok(quizService.update(quizRecord, quizRecord.getProfession().getId()));
    } catch (Exception ex) {
      log.error(String.format("Error while updating the quiz %s", ex.getMessage()));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              "There was a problem while creating the quiz."
                  + "Check the values provided or please try again later");
    }
  }

  @PutMapping("/publish")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity publish(@Valid @RequestBody QuizDto quizDto) {
    try {
      return ResponseEntity.ok(quizService.publish(quizDTOFactory.mapDtoToModel(quizDto)));
    } catch (QuizServiceException qe) {
      log.error(String.format("Error while publishing the quiz %s", qe.getMessage()));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              "There was a problem while publishing the quiz."
                  + "Check the values provided or please try again later");
    }
  }

  @PostMapping("/submit-answers")
  @RoleMapping(Roles.LEARNER)
  public ResponseEntity submitQuiz(@Valid @RequestBody QuizSubmissionDto quizSubmissionDto) {
    try {
      return quizService
          .submitAnswers(quizSubmissionDto)
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.badRequest().build());
    } catch (QuizServiceException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getLocalizedMessage());
    }
  }

  @GetMapping("/quiz-summary")
  @RoleMapping(Roles.LEARNER)
  public ResponseEntity getQuizResult(
      @RequestParam Long quizResultId, @RequestParam String staffId) {
    try {
      return ok(quizService.getQuizResult(quizResultId, staffId));
    } catch (QuizServiceException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              "Failed getting quiz result."
                  + "Please check the values passed or try again later. "
                  + ex);
    }
  }

  @GetMapping("/quiz-history")
  @RoleMapping(Roles.LEARNER)
  public ResponseEntity getQuizHistoryForStaff(@RequestParam String staffId) {

    return quizService
        .getQuizHistory(staffId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/results-by-profession")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity getQuizResultsForProfession(
      @RequestParam int professionId) {

    return quizService
        .getAllResultsForProfession(professionId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/results-for-your-org")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity getQuizResultsForOrg(@RequestParam int organisationId) {

    return quizService
        .getForAllProfessionsInTheOrganisation(organisationId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/{professionId}")
  @RoleMapping({
    Roles.LEARNING_MANAGER,
    Roles.CSHR_REPORTER,
    Roles.ORGANISATION_REPORTER,
    Roles.PROFESSION_REPORTER
  })
  public ResponseEntity getQuizForProfession(@PathVariable int professionId) {

    return quizService
        .getQuiz(professionId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/{professionId}/info")
  @RoleMapping(Roles.LEARNER)
  public ResponseEntity getQuizMetaData(@PathVariable Long professionId) {

    return quizService
        .getQuizInfo(professionId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @DeleteMapping("/delete-results")
  @RoleMapping({Roles.CSHR_REPORTER, Roles.LEARNING_MANAGER})
  public ResponseEntity deleteResults(
      @RequestParam(value = "resultId", required = false) long resultId,
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    LocalDateTime fromDate = LocalDateTime.of(from, LocalTime.now());

    LocalDateTime toDate = LocalDateTime.of(to, LocalTime.MAX);
    return ok(quizService.deleteQuizResultsBetween(fromDate, toDate));
  }
}
