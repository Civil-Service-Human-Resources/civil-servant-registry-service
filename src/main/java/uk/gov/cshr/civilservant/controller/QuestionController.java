package uk.gov.cshr.civilservant.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.civilservant.dto.AddQuestionDto;
import uk.gov.cshr.civilservant.dto.QuestionDto;
import uk.gov.cshr.civilservant.exception.QuizNotFoundException;
import uk.gov.cshr.civilservant.exception.QuizServiceException;
import uk.gov.cshr.civilservant.mapping.RoleMapping;
import uk.gov.cshr.civilservant.service.QuestionService;

@Slf4j
@RestController
@RequestMapping(QuestionController.QUESTION_ROOT)
public class QuestionController {

  static final String QUESTION_ROOT = "/api/questions";

  private final QuestionService questionService;

  @Autowired
  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @Transactional
  @RoleMapping({
    "LEARNING_MANAGER",
    "CSHR_REPORTER",
    "ORGANISATION_REPORTER",
    "PROFESSION_REPORTER"
  })
  @PostMapping("/add-question")
  public ResponseEntity addQuestion(@Valid @RequestBody AddQuestionDto addQuestionDto) {
    try {
      long professionId = addQuestionDto.getProfessionId().longValue();
      QuestionDto questionDto = addQuestionDto.getQuestion();

      return ResponseEntity.ok(
          questionService.addQuizQuestion(professionId, questionDto));
    } catch (QuizServiceException ex) {
      log.error("Error while adding question to Quiz {}", ex);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("There was a problem adding the question. Error: " + ex.getLocalizedMessage());
    } catch (QuizNotFoundException ex) {
      log.error("Error while adding question to Quiz {}", ex);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("There was a problem adding the question. Error: " + ex.getLocalizedMessage());
    }
  }

  @Transactional
  @RoleMapping({
    "LEARNING_MANAGER",
    "CSHR_REPORTER",
    "ORGANISATION_REPORTER",
    "PROFESSION_REPORTER"
  })
  @PostMapping("/update")
  public ResponseEntity updateQuestion(@Valid @RequestBody QuestionDto questionDTO) {
    try {
      return ResponseEntity.ok(questionService.updateQuizQuestion(questionDTO));
    } catch (Exception ex) {
      log.error("Updating record failed {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("There was a problem updating the quiz. Check the values provided");
    }
  }

  @RoleMapping({
    "LEARNING_MANAGER",
    "CSHR_REPORTER",
    "ORGANISATION_REPORTER",
    "PROFESSION_REPORTER"
  })
  @DeleteMapping("/{id}/delete")
  public ResponseEntity delete(@PathVariable Long id) {
    try {
      log.info(String.format("Deleting a question with id %d", id));
      questionService.deleteQuestion(id);
    } catch (Exception ex) {
      log.error("Updating record failed {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("There was a problem updating the quiz");
    }
    return ResponseEntity.ok().build();
  }

  @RoleMapping({
    "LEARNING_MANAGER",
    "CSHR_REPORTER",
    "ORGANISATION_REPORTER",
    "PROFESSION_REPORTER"
  })
  @GetMapping("/{id}/preview")
  public ResponseEntity preview(@PathVariable Long id) {
    log.debug(String.format("Preview requested for question %d", id));
    return questionService
        .getById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
