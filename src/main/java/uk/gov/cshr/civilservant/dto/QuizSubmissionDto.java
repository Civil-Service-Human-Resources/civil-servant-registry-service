package uk.gov.cshr.civilservant.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuizSubmissionDto {
  String staffId;
  Integer quizId;
  Integer professionId;
  Integer organisationId;
  String quizName;
  SubmittedAnswerDto[] answers;
}
