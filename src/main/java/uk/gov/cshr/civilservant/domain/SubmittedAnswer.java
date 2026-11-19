package uk.gov.cshr.civilservant.domain;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import uk.gov.cshr.civilservant.mapping.SubmittedAnswersConverter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubmittedAnswer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Convert(converter = SubmittedAnswersConverter.class)
  @Column
  private String[] submittedAnswers;

  private String question;

  @ManyToOne
  @JsonBackReference
  @JoinColumn(name = "quiz_result_id")
  private QuizResult quizResult;

  @Column private boolean skipped;

  @Column private boolean correct;

  public void setQuizResult(QuizResult quizResult) {
    if (quizResult != null) {
      quizResult.getAnswers().add(this);
    } else if (this.quizResult != null) {
      this.quizResult.getAnswers().add(null);
    }
    this.quizResult = quizResult;
  }
}
