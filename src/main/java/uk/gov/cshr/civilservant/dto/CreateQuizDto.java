package uk.gov.cshr.civilservant.dto;

import lombok.*;

import jakarta.validation.constraints.NotNull;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateQuizDto {
  @NotNull ProfessionDto profession;
}
