package uk.gov.cshr.civilservant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.cshr.civilservant.domain.Grade;
import uk.gov.cshr.civilservant.repository.GradeRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradeService{
    public final GradeRepository gradeRepository;

    public Optional<Grade> getGradeById(Long id) {
        return gradeRepository.findById(id);
    }
}
