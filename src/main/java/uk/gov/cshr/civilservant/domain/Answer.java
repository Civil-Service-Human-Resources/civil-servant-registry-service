package uk.gov.cshr.civilservant.domain;

import java.util.Map;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.mapping.ToOne;
import uk.gov.cshr.civilservant.mapping.AnswerMapConverter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "No answer text provided.")
  @Column(nullable = false)
  private String correctAnswer;

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "question_id")
  private Question question;

  @Convert(converter = AnswerMapConverter.class)
  @Column
  private Map<String, Object> answers;

  public void setQuestion(Question question) {
    if (question != null) {
      question.setAnswer(this);
    } else if (this.question != null) {
      this.question.setAnswer(null);
    }
    this.question = question;
  }
}
