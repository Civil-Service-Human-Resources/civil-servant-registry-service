package uk.gov.cshr.civilservant.controller.v2.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.Max;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableParams {
    int pageIndex = 0;

    @Max(200)
    int pageSize = 20;

    public Pageable getAsPageable() {
        return PageRequest.of(pageIndex, pageSize);
    }
}
