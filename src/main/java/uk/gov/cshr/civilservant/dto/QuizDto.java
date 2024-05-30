package uk.gov.cshr.civilservant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import uk.gov.cshr.civilservant.domain.Status;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizDto {
  private Long id;
  private String name;
  private ProfessionDto profession;
  private Set<QuestionDto> questions;
  private LocalDateTime createdOn;
  private LocalDateTime updatedOn;
  private String result;
  private Status status;
  private int numberOfQuestions;
  private String description;
  private int numberOfAttempts;
  private float averageScore;
}
