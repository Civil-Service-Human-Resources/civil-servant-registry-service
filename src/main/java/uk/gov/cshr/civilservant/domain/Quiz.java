package uk.gov.cshr.civilservant.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quiz {

  @Column String result;
  @Column Integer numberOfQuestions;

  @Column
  @Enumerated(EnumType.STRING)
  Status status;

  @Column String description;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column private String name;

  @OneToOne(fetch = FetchType.EAGER)
  private Profession profession;

  @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JsonManagedReference
  @Builder.Default
  private Set<Question> questions = new HashSet<>();

  @CreationTimestamp private LocalDateTime createdOn;
  @UpdateTimestamp private LocalDateTime updatedOn;
}
