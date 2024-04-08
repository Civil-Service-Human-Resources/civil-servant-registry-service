package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.civilservant.domain.Grade;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeDto {
    private Long id;
    private String code;
    private String name;

    public static GradeDto fromGrade(Grade grade) {
        return new GradeDto(grade.getId(), grade.getCode(), grade.getName());
    }
}
