package uk.gov.cshr.civilservant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.AnswerDtoFactory;
import uk.gov.cshr.civilservant.dto.factory.QuizDtoFactory;
import uk.gov.cshr.civilservant.dto.factory.QuizResultDtoFactory;
import uk.gov.cshr.civilservant.exception.ProfessionNotFoundException;
import uk.gov.cshr.civilservant.exception.QuizNotFoundException;
import uk.gov.cshr.civilservant.exception.QuizServiceException;
import uk.gov.cshr.civilservant.repository.ProfessionRepository;
import uk.gov.cshr.civilservant.repository.QuizRepository;
import uk.gov.cshr.civilservant.repository.QuizResultRepository;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(
    propagation = Propagation.REQUIRES_NEW,
    rollbackFor = {QuizServiceException.class, QuizNotFoundException.class})
public class QuizService {

  private final AnswerDtoFactory answerDtoFactory;
  private QuestionService questionService;
  private QuizRepository quizRepository;
  private ProfessionRepository professionRepository;
  private ProfessionService professionService;
  private QuizResultRepository quizResultRepository;
  private QuizDtoFactory quizDtoFactory;
  private QuizResultDtoFactory quizResultDtoFactory;
  private ObjectMapper objectMapper;

  @Autowired
  public QuizService(
          QuizRepository quizRepository,
          QuizDtoFactory quizDtoFactory,
          ProfessionRepository professionRepository,
          QuestionService questionService,
          QuizResultRepository quizResultRepository,
          AnswerDtoFactory answerDtoFactory,
          ProfessionService professionService, QuizResultDtoFactory quizResultDtoFactory,
          ObjectMapper objectMapper) {
    this.quizRepository = quizRepository;
    this.quizDtoFactory = quizDtoFactory;
    this.professionRepository = professionRepository;
    this.questionService = questionService;
    this.quizResultRepository = quizResultRepository;
    this.answerDtoFactory = answerDtoFactory;
    this.professionService = professionService;
    this.quizResultDtoFactory = quizResultDtoFactory;
    this.objectMapper = objectMapper;
  }

  public Optional<QuizDto> getQuizByProfessionId(Long professionId) {
    Optional<Quiz> quiz =
        quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE);
    if (quiz.isPresent()) {
      final Set<Question> questionsToBeRemoved = collectDeletedQuestionsInAQuiz(quiz);
      quiz.get().getQuestions().removeAll(questionsToBeRemoved);
      return Optional.of(quizDtoFactory.create(quiz.get()));
    }
    return Optional.empty();
  }

  @Transactional
  public void delete(Long professionId) {
    Optional<Quiz> optionalEntry =
        quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE);
    if (!optionalEntry.isPresent()) {
      throw new EntityNotFoundException("No quiz found matching the id provided");
    } else {
      Quiz quizTobeDeleted = optionalEntry.get();
      quizTobeDeleted.setStatus(Status.INACTIVE);
      quizTobeDeleted.getQuestions().forEach(question -> question.setStatus(Status.INACTIVE));
      quizRepository.save(quizTobeDeleted);
    }
  }

  public QuizDto save(Quiz quiz) {
    return quizDtoFactory.create(quizRepository.save(quiz));
  }

  public QuizDto update(Quiz quiz, Long professionId) {
    Optional<Quiz> quizToBeUpdated =
        quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE);
    if (quizToBeUpdated.isPresent()) {
      Quiz quizEntry = quizToBeUpdated.get();
      quizEntry.setDescription(quiz.getDescription());
      quizEntry.setUpdatedOn(LocalDateTime.now());
      quizEntry = quizRepository.save(quizEntry);
      return quizDtoFactory.create(quizEntry);
    }
    return null;
  }

  public QuizDto create(Long professionId) throws ProfessionNotFoundException {
    Optional<QuizDto> quizDto = getQuizByProfessionId(professionId);
    if (!quizDto.isPresent()) {
      Profession profession = professionService.getWithId(professionId);
      return quizDtoFactory.create(
          quizRepository.save(
              Quiz.builder()
                  .status(Status.DRAFT)
                  .profession(profession)
                  .description("")
                  .createdOn(LocalDateTime.now())
                  .updatedOn(LocalDateTime.now())
                  .name("Quiz for " + profession.getName())
                  .numberOfQuestions(0)
                  .build()));
    }
    return quizDto.get();
  }

  public Optional<QuizDto> getQuizInfo(Long professionId) {

    Optional<Quiz> quizRecord =
        quizRepository.findFirstByProfessionIdAndStatusIs(professionId, Status.PUBLISHED);
    if (quizRecord.isPresent()) {
      final Set<Question> questionsToBeRemoved = collectDeletedQuestionsInAQuiz(quizRecord);
      quizRecord.get().getQuestions().removeAll(questionsToBeRemoved);
      quizRecord.get().setNumberOfQuestions(quizRecord.get().getQuestions().size());
      return Optional.ofNullable(quizDtoFactory.mapSpecificFields(quizRecord.get()));
    }
    return Optional.empty();
  }

  public long publish(Quiz quiz) throws QuizServiceException {
    Optional<Quiz> quizToBePublished =
        quizRepository.findFirstByProfessionIdAndStatusIsNot(
            quiz.getProfession().getId(), Status.INACTIVE);
    if (quizToBePublished.isPresent()) {
      Set<Question> activeQuestions =
          quizToBePublished
              .get()
              .getQuestions()
              .stream()
              .filter(question -> !question.getStatus().equals(Status.INACTIVE))
              .collect(Collectors.toSet());
      if (activeQuestions.size() > 17) {
        quizToBePublished.get().setStatus(Status.PUBLISHED);
        quizToBePublished.get().setUpdatedOn(LocalDateTime.now());
        return save(quizToBePublished.get()).getId();
      } else {
        throw new QuizServiceException(
            "Quiz cannot be published. "
                + "A quiz must have 18 or more active questions before it can be published");
      }
    } else {
      throw new QuizServiceException("Quiz does not exist");
    }
  }

  public Optional<Long> submitAnswers(QuizSubmissionDto quizSubmissionDto)
      throws QuizServiceException {
    try {
      QuizResult result =
          QuizResult.builder()
              .quizId(quizSubmissionDto.getQuizId())
              .staffId(quizSubmissionDto.getStaffId())
              .professionId(quizSubmissionDto.getProfessionId())
              .organisationId(quizSubmissionDto.getOrganisationId())
              .quizName(quizSubmissionDto.getQuizName())
              .answers(new ArrayList<>())
              .type(quizSubmissionDto.getAnswers().length > 18 ? QuizType.LONG : QuizType.SHORT)
              .build();

      int correctCount = 0;

      for (SubmittedAnswerDto answersDto : quizSubmissionDto.getAnswers()) {
        correctCount = calculateResults(result, correctCount, answersDto);
      }

      result.setCorrectAnswers(correctCount);
      result.setNumberOfQuestions(quizSubmissionDto.getAnswers().length);
      result.setScore((correctCount * 100) / result.getNumberOfQuestions());
      result.setCompletedOn(LocalDateTime.now());
      return Optional.of(quizResultRepository.save(result).getId());
    } catch (Exception ex) {
      throw new QuizServiceException("Quiz submission failed " + ex.getMessage());
    }
  }

  @Transactional
  protected int calculateResults(QuizResult result, int correctCount, SubmittedAnswerDto answersDto)
      throws JsonProcessingException {

    Optional<Question> question =
        questionService.getByQuestionId((long) answersDto.getQuestionId());

    if (question.isPresent()) {
      SubmittedAnswer submittedAnswer = SubmittedAnswer.builder().build();
      if (!answersDto.isSkipped()) {
        AnswerDto answerDto = answerDtoFactory.create(question.get().getAnswer());
        if (answerDto != null) {
          if (Arrays.deepEquals(answersDto.getSubmittedAnswers(), answerDto.getCorrectAnswers())) {
            submittedAnswer.setCorrect(true);
            correctCount++;
          } else {
            submittedAnswer.setCorrect(false);
          }
        }
        submittedAnswer.setSubmittedAnswers(answersDto.getSubmittedAnswers());
        submittedAnswer.setSkipped(false);
      } else {
        submittedAnswer.setSubmittedAnswers(new String[] {});
        submittedAnswer.setSkipped(true);
      }
      submittedAnswer.setQuizResult(result);
      submittedAnswer.setQuestion(objectMapper.writeValueAsString(question.get()));
      result.getAnswers().add(submittedAnswer);
    }
    return correctCount;
  }

  public QuizResultDto getQuizResult(Long quizResultId, String staffId)
      throws QuizServiceException {
    Optional<QuizResult> quizResultOptional =
        quizResultRepository.findQuizResultByIdAndStaffId(quizResultId, staffId);

    return quizResultOptional
        .map(quizResultDtoFactory::create)
        .orElseThrow(() -> new QuizServiceException("Failed to get results"));
  }

  public Optional<QuizHistoryDto> getQuizHistory(String staffId) {
    QuizHistoryDto history = QuizHistoryDto.builder().build();
    List<QuizResultDto> listOfQuizzesByUser = quizResultRepository.findQuizResultByStaffId(staffId);

    if (listOfQuizzesByUser != null && !listOfQuizzesByUser.isEmpty()) {
      history.setQuizResultDto(listOfQuizzesByUser);
    } else {
      return Optional.empty();
    }
    return Optional.of(history);
  }

  public Optional<List<QuizDataTableDto>> getAllResults() {
    List<QuizDataTableDto> quizDataTableDtoList = new ArrayList<>();
    List<QuizResultSummaryDto> quizResultSummaryDtos = quizResultRepository.findAllResults();

    populateDataTableDtos(quizDataTableDtoList, quizResultSummaryDtos);
    return Optional.of(quizDataTableDtoList);
  }

  public Optional<QuizDataTableDto> getAllResultsForProfession(
      long professionId) {
    Optional<Profession> profession = professionRepository.findById(professionId);
    if (profession.isPresent()) {
      QuizResultSummaryDto quizResultDto =
          quizResultRepository.findByProfessionId(professionId);

      if (quizResultDto != null) {
        return Optional.ofNullable(populateDataTableDto(quizResultDto, profession.get().getName()));
      } else {
        return Optional.of(
            QuizDataTableDto.builder()
                .profession(profession.get().getName())
                .numberOfAttempts(0)
                .averageScore("")
                .build());
      }
    }
    return Optional.empty();
  }

  public Optional<QuizDto> getQuiz(long professionId) {

    Optional<Quiz> quiz =
        quizRepository.findFirstByProfessionIdAndStatusIsNot(professionId, Status.INACTIVE);

    if (quiz.isPresent()) {
      final Set<Question> inactiveQuestions = collectDeletedQuestionsInAQuiz(quiz);
      quiz.get().getQuestions().removeAll(inactiveQuestions);
    }
    return quiz.map(quizDtoFactory::create);
  }

  public Optional<List<QuizDataTableDto>> getForAllProfessionsInTheOrganisation(
      long organisationId) {
    List<QuizDataTableDto> quizDataTableDtoList = new ArrayList<>();
    List<QuizResultSummaryDto> quizResultSummaryDtoList =
        quizResultRepository.findByOrOrganisationIdOrderByQuizNameAsc(organisationId);

    if (!quizResultSummaryDtoList.isEmpty()) {
      populateDataTableDtos(quizDataTableDtoList, quizResultSummaryDtoList);
    }
    return Optional.of(quizDataTableDtoList);
  }

  private Set<Question> collectDeletedQuestionsInAQuiz(Optional<Quiz> quiz) {
    return quiz.get()
        .getQuestions()
        .stream()
        .filter(question -> question.getStatus().equals(Status.INACTIVE))
        .collect(Collectors.toSet());
  }

  private void populateDataTableDtos(
      List<QuizDataTableDto> quizDataTableDtoList,
      List<QuizResultSummaryDto> quizResultSummaryDtos) {
    for (QuizResultSummaryDto quizResultSummaryDto : quizResultSummaryDtos) {

      final Optional<Profession> profession =
          professionRepository.findById(quizResultSummaryDto.getProfessionId());
      String professionName = "";
      if (profession.isPresent()) {
        professionName = profession.get().getName();
      }

      final QuizDataTableDto quizDataTableDto =
          populateDataTableDto(quizResultSummaryDto, professionName);

      quizDataTableDtoList.add(quizDataTableDto);
    }
  }

  private QuizDataTableDto populateDataTableDto(
      QuizResultSummaryDto quizResultSummaryDto, String professionName) {
    String bd = "0.00";
    DecimalFormat dec = new DecimalFormat("#.00");
    if (quizResultSummaryDto.getAverageScore() != null
    && quizResultSummaryDto.getAverageScore() != 0d) {
       bd = dec.format(new BigDecimal(quizResultSummaryDto.getAverageScore()).setScale(2, RoundingMode.CEILING));
    }
    return QuizDataTableDto.builder()
        .profession(professionName)
        .averageScore(bd)
        .numberOfAttempts((int) quizResultSummaryDto.getNumberOfAttempts())
        .build();
  }

  public List<Long> deleteQuizResultsBetween(LocalDateTime from, LocalDateTime to) {
    List<QuizResult> quizResults = quizResultRepository.findAllByCompletedOnBetween(from, to);
    List<Long> resultsQualifiedForDeletion =
        quizResults.stream().map(QuizResult::getId).collect(Collectors.toList());
    quizResultRepository.deleteAll(quizResults);
    return resultsQualifiedForDeletion;
  }

  /** Methods for report extraction */
  public List<SkillsReportsDto> getReportForSuperAdmin(LocalDateTime from, LocalDateTime to) {
    List<QuizResult> quizResults = quizResultRepository.findAllByCompletedOnBetween(from, to);

    return extractReportFromResult(quizResults);
  }

  public List<SkillsReportsDto> getReportForOrganisationAdmin(
      long organisationId, LocalDateTime from, LocalDateTime to) {
    List<QuizResult> quizResults =
        quizResultRepository.findAllByOrganisationIdAndCompletedOnBetween(organisationId, from, to);

    return extractReportFromResult(quizResults);
  }

  public List<SkillsReportsDto> getReportForProfessionReporter(
      long organisationId, long professionId, LocalDateTime from, LocalDateTime to) {
    List<QuizResult> quizResults =
        quizResultRepository.findAllByOrganisationIdAndProfessionIdAndCompletedOnBetween(
            organisationId, professionId, from, to);

    return extractReportFromResult(quizResults);
  }

  public List<SkillsReportsDto> getReportForProfessionAdmin(
      long professionId, LocalDateTime from, LocalDateTime to) {
    List<QuizResult> quizResults =
        quizResultRepository.findAllByProfessionIdAndCompletedOnBetween(professionId, from, to);

    return extractReportFromResult(quizResults);
  }

  public long deleteQuizResultsCompletedBeforeDate(LocalDateTime before) {
    return quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(before);
  }

  private List<SkillsReportsDto> extractReportFromResult(List<QuizResult> quizResults) {

    Map<Long, QuestionMetrics> metricsMap = new HashMap<>();

    quizResults.forEach(
        quizResult ->
            quizResult
                .getAnswers()
                .forEach(
                    submittedAnswer -> {
                      try {
                        Long questionId = objectMapper
                                .readValue(submittedAnswer.getQuestion(), Question.class)
                                .getId();
                          if (!metricsMap.containsKey(questionId)) {
                              metricsMap.put(questionId,new QuestionMetrics());
                          }
                          calculateMetrics(metricsMap.get(questionId), submittedAnswer);

                      } catch (IOException e) {
                        log.error("Reading from question failed {}", e.getMessage());
                      }
                    }));
    return populateQuestionData(metricsMap);

  }

  private void calculateMetrics(QuestionMetrics questionMetrics, SubmittedAnswer submittedAnswer) {
    int correct = questionMetrics.getCorrectCount();
    int incorrect = questionMetrics.getIncorrectCount();
    int skipped = questionMetrics.getSkippedCount();
    int timesAttempted = questionMetrics.getTimesAttempted();
    if (submittedAnswer.isCorrect()) {
      correct++;
      questionMetrics.setCorrectCount(correct);
    } else {
      incorrect++;
      questionMetrics.setIncorrectCount(incorrect);
    }

    if (submittedAnswer.isSkipped()) {
      skipped++;
      questionMetrics.setSkippedCount(skipped);
    }

    timesAttempted++;
    questionMetrics.setTimesAttempted(timesAttempted);
  }

  private List<SkillsReportsDto> populateQuestionData(
          Map<Long, QuestionMetrics> metricsMap) {
    List<Question> questionList = questionService.findAll(metricsMap.keySet());
    List<SkillsReportsDto> skillsReportsDtoList = new ArrayList<>();

    questionList.forEach(
            question -> {
              final SkillsReportsDto skillsReportsDto =
                      SkillsReportsDto.builder()
                              .quizName(question.getQuiz().getName())
                              .status(question.getQuiz().getStatus())
                              .professionName(question.getQuiz().getProfession().getName())
                              .questionName(question.getValue())
                              .questionTheme(question.getTheme())
                              .questionId(question.getId())
                              .timesAttempted(metricsMap.get(question.getId()).getTimesAttempted())
                              .incorrectCount(metricsMap.get(question.getId()).getIncorrectCount())
                              .correctCount(metricsMap.get(question.getId()).getCorrectCount())
                              .skippedCount(metricsMap.get(question.getId()).getSkippedCount())
                              .build();
              skillsReportsDtoList.add(skillsReportsDto);
            });
    return skillsReportsDtoList;
  }
}
