package uk.gov.cshr.civilservant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import uk.gov.cshr.civilservant.domain.Profession;
import uk.gov.cshr.civilservant.domain.Quiz;
import uk.gov.cshr.civilservant.domain.QuizResult;
import uk.gov.cshr.civilservant.domain.Status;
import uk.gov.cshr.civilservant.dto.QuizResultDto;
import uk.gov.cshr.civilservant.dto.QuizResultSummaryDto;


@DataJpaTest
public class QuizResultRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ProfessionRepository professionRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    private Quiz testQuiz, anotherQuiz;

    @BeforeEach
    public void setupQuizData() {
        Profession testProfession = new Profession();
        testProfession.setName("Test Profession - Quiz Result Test");
        testProfession = professionRepository.save(testProfession);

        Quiz testQuiz = Quiz.builder().name("Test Quiz - Quiz Result Test").status(Status.PUBLISHED).profession(testProfession).build();
        this.testQuiz = quizRepository.save(testQuiz);
        this.anotherQuiz = quizRepository.save(
                Quiz.builder()
                        .name("Test Quiz for Analysis")
                        .profession(
                                professionRepository.findById(1L).get()
                        )
                        .status(Status.PUBLISHED)
                        .build());
    }

    @AfterEach
    public void clearQuizData() {
        quizRepository.deleteAll();
    }

    @Test
    public void deleteQuizResultsByCompletedOnIsLessThanEqual_resultRecordCompletedBeforeSuppliedTimeShouldBeDeleted() {
        quizResultRepository.save(QuizResult.builder().quizId(this.testQuiz.getId()).correctAnswers(1).build());
        List<QuizResult> quizResult = quizResultRepository.findAll();
        assertEquals(1, quizResult.size());

        long deleteCount = quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(LocalDateTime.now().plusHours(1));
        assertEquals(1, deleteCount);

        quizResult = quizResultRepository.findAll();
        assertEquals(0, quizResult.size());
    }

    @Test
    public void deleteQuizResultsByCompletedOnIsLessThanEqual_resultRecordCompletedAfterSuppliedTimeShouldNotBeDeleted() {
        quizResultRepository.save(QuizResult.builder().quizId(this.testQuiz.getId()).correctAnswers(1).build());
        List<QuizResult> quizResult = quizResultRepository.findAll();
        assertEquals(1, quizResult.size());

        long deleteCount = quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(LocalDateTime.now().minusHours(1));
        assertEquals(0, deleteCount);

        quizResult = quizResultRepository.findAll();
        assertEquals(1, quizResult.size());
    }

    @Test
    public void deleteQuizResultsByCompletedOnIsLessThanEqual_mix() throws InterruptedException {
        quizResultRepository.save(QuizResult.builder().quizId(this.testQuiz.getId()).correctAnswers(1).build());
        // Default MySQL TIMESTAMP resolution is seconds, ensure at least one second has past to have different timestamps
        Thread.sleep(1001);
        LocalDateTime postWaitTime = LocalDateTime.now();
        Thread.sleep(1001);
        quizResultRepository.save(QuizResult.builder().quizId(this.testQuiz.getId()).correctAnswers(2).build());

        List<QuizResult> quizResult = quizResultRepository.findAll();
        assertEquals(2, quizResult.size());

        long deleteCount = quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(postWaitTime);
        assertEquals(1, deleteCount);

        quizResult = quizResultRepository.findAll();
        assertEquals(1, quizResult.size());
    }

    @Test
    public void deleteQuizResultsByCompletedOnIsLessThanEqual_resultRecordCompletedAtSuppliedTimeShouldBeDeleted() {
        QuizResult quizResult = quizResultRepository.save(QuizResult.builder().quizId(this.testQuiz.getId()).correctAnswers(1).build());
        List<QuizResult> allQuizResults = quizResultRepository.findAll();
        assertEquals(1, allQuizResults.size());

        long deleteCount = quizResultRepository.deleteQuizResultsByCompletedOnIsLessThanEqual(quizResult.getCompletedOn());
        assertEquals(1, deleteCount);

        allQuizResults = quizResultRepository.findAll();
        assertEquals(0, allQuizResults.size());
    }

    @Test
    public void shouldReturnQuizResults_ByStaffId() {
        //given

        QuizResult quizResult = quizResultRepository.save(
                QuizResult.builder().quizId(
                        this.anotherQuiz.getId())
                        .correctAnswers(5)
                        .score(80)
                        .staffId("1238")
                        .professionId(1L)
                        .build());

        final String staffId = "1238";

        //when

        final List<QuizResultDto> quizResultDtoList =
                quizResultRepository.findQuizResultByStaffId(staffId);

        //then
        assertTrue(quizResultDtoList.size() > 0);
    }

    @Test
    public void shouldReturnQuizResults_ByProfessionId() {
        //given
        QuizResult quizResult = quizResultRepository.save(
                QuizResult.builder().quizId(
                        this.anotherQuiz.getId())
                        .correctAnswers(5)
                        .score(80)
                        .professionId(1L)
                        .build());

        final long professionId = 1L;

        //when

        final QuizResultSummaryDto quizResultSummaryDto =
                quizResultRepository.findByProfessionId(professionId);

        //then
        assertTrue(quizResultSummaryDto.getAverageScore() > 0.0);
        assertEquals(1, quizResultSummaryDto.getProfessionId());
        assertTrue(quizResultSummaryDto.getNumberOfAttempts() > 0);
    }

    @Test
    public void shouldReturnQuizResults_findByOrOrganisationIdOrderByQuizNameAsc() {
        //given
        List<QuizResult> quizResult = new ArrayList<>();
        quizResult.add(QuizResult.builder().quizId(
                this.testQuiz.getId())
                .correctAnswers(5)
                .score(80)
                .professionId(2L)
                .organisationId(1L)
                .build());
        quizResult.add(QuizResult.builder().quizId(
                this.anotherQuiz.getId())
                .correctAnswers(6)
                .score(90)
                .professionId(1L)
                .organisationId(1L)
                .build());
        quizResultRepository.saveAll(quizResult);

        final long organisationId = 1L;

        //when

        final List<QuizResultSummaryDto> quizResultSummaryDtoList =
                quizResultRepository.findByOrOrganisationIdOrderByQuizNameAsc(organisationId);

        //then

        assertEquals(2, quizResultSummaryDtoList.size());
        // assert the second element in the list to ensure ordering by quiz name.
        assertEquals(2, quizResultSummaryDtoList.get(1).getProfessionId());

    }

    @Test
    public void shouldReturnQuizResults_findAllResults() {
        //given
        QuizResult quizResult = quizResultRepository.save(
                QuizResult.builder().quizId(
                        this.anotherQuiz.getId())
                        .correctAnswers(5)
                        .score(80)
                        .professionId(1L)
                        .organisationId(1L)
                        .build());
        //when

        final List<QuizResultSummaryDto> quizResultSummaryDtoList =
                quizResultRepository.findAllResults();

        //then

        assertTrue(quizResultSummaryDtoList.size() > 0);

        for (QuizResultSummaryDto quizResultSummaryDto : quizResultSummaryDtoList) {
            assertTrue(quizResultSummaryDto.getAverageScore() > 0.0);
            assertEquals(1, quizResultSummaryDto.getProfessionId());
            assertTrue(quizResultSummaryDto.getNumberOfAttempts() > 0);
        }
    }
}
