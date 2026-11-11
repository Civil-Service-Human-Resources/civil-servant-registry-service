package uk.gov.cshr.civilservant.mapping;

import jakarta.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmittedAnswersConverter implements AttributeConverter<String[], String> {

  private static final Logger logger = LoggerFactory.getLogger(AnswerMapConverter.class);

  @Override
  public String convertToDatabaseColumn(String[] answers) {
    String submittedAnswers = "";
    try {
      submittedAnswers = String.join(",", answers);
    } catch (Exception e) {
      logger.error("String processing failed : Reason -> ", e.getMessage());
    }
    return submittedAnswers;
  }

  @Override
  public String[] convertToEntityAttribute(String answers) {
    String[] submittedAnswers = null;
    try {
      submittedAnswers = answers.split(",");
    } catch (Exception e) {
      logger.error("Data read error : Reason -> ", e.getMessage());
    }
    return submittedAnswers;
  }
}
