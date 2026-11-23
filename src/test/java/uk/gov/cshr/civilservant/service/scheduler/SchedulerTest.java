package uk.gov.cshr.civilservant.service.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.cshr.civilservant.service.QuizService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {

    private static final Integer testWindow = Integer.valueOf(1095);

    @Mock
    QuizService quizService;

    @InjectMocks
    Scheduler scheduler;

    @BeforeEach
    public void setWindowValue() {
        ReflectionTestUtils.setField(scheduler, "retentionWindowInDays", testWindow);
    }

    @Test
    public void skillsDataRetention() {
        when(quizService.deleteQuizResultsCompletedBeforeDate(any())).thenReturn(1L);

        scheduler.skillsDataRetention();

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(quizService).deleteQuizResultsCompletedBeforeDate(dateCaptor.capture());
        verifyNoMoreInteractions(quizService);

        LocalDateTime calculatedPassedDateTime = LocalDateTime.now().minusDays(testWindow);
        LocalDateTime passedDateTime = dateCaptor.getValue();

        long passedCalculatedDiff = calculatedPassedDateTime.toInstant(ZoneOffset.UTC).getEpochSecond() - passedDateTime.toInstant(ZoneOffset.UTC).getEpochSecond();

        // Calculated timestamp is created after execution so there should be a small difference, working in seconds so could be 0
        assertTrue(passedCalculatedDiff >= 0);

        // Check the difference was less than 5 seconds (should be less but add small gap)
        assertTrue(passedCalculatedDiff < 5);
    }
}