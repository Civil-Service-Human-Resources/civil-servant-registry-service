package uk.gov.cshr.civilservant.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc

@WithMockUser(username = "user")
public class ValidAgencyTokenCapacityValidatorTest {

    @Autowired
    private ValidAgencyTokenCapacityValidator classUnderTest;

    @Value("${agencyToken.capacity.min}")
    private int minValueFromConfig;

    @Value("${agencyToken.capacity.max}")
    private int maxValueFromConfig;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class))
                .thenReturn(hibernateContext);
    }


    @Test
    public void givenAValidCapacity_whenValidated_thenAgencyTokenDTOValidationPasses() {
        // given
        Integer capacity = minValueFromConfig + 1;

        // when
        boolean actual = classUnderTest.isValid(capacity, constraintValidatorContext);

        // then
        assertTrue(actual);
    }

    @Test
    public void givenAnInValidCapacityGreaterThanMax_whenValidated_thenAgencyTokenDTOValidationFails() {
        Integer capacity = maxValueFromConfig + 1;

        boolean actual = classUnderTest.isValid(capacity, constraintValidatorContext);

        assertFalse(actual);
    }

    @Test
    public void givenAnInValidCapacityLessThanMin_whenValidated_thenAgencyTokenDTOValidationFails() {
        // given
        Integer capacity = minValueFromConfig - 1;

        // when
        boolean actual = classUnderTest.isValid(capacity, constraintValidatorContext);

        // then
        assertFalse(actual);
    }

}
