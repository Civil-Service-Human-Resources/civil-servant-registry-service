package uk.gov.cshr.civilservant.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private QuestionType type;

  @Column(length = 200)
  @Size(min = 3, max = 200)
  private String theme;

  @Column(length = 500)
  @Size(min = 10, max = 500)
  private String value;

  @Column(length = 500)
  private String why;

  @Column(length = 500)
  private String learningName;

  @Column(length = 500)
  private String learningReference;

  @ManyToOne(fetch = FetchType.EAGER)
  @JsonBackReference
  @JoinColumn(name = "quiz_id")
  private Quiz quiz;

  @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
  private Answer answer;

  @Column private String imgUrl;

  @Column(length = 500)
  private String suggestions;

  @Column
  @Enumerated(EnumType.STRING)
  private Status status;

  @Column private String alternativeText;

  /** Below fields are used in reporting. */
  @Column private long timesAttempted;

  @Column private long correctCount;

  @Column private long incorrectCount;

  @Column private long skippedCount;

  public void setQuiz(Quiz quiz) {
    if (quiz != null) {
      quiz.getQuestions().add(this);
    } else if (this.quiz != null) {
      this.quiz.getQuestions().remove(this);
    }
    this.quiz = quiz;
  }
}
