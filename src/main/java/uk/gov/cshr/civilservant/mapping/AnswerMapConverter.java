package uk.gov.cshr.civilservant.mapping;

import java.io.IOException;
import java.util.Map;
import jakarta.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerMapConverter implements AttributeConverter<Map<String, Object>, String> {

  private static final Logger logger = LoggerFactory.getLogger(AnswerMapConverter.class);
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Object> answers) {
    String answerSetJson = null;
    try {
      answerSetJson = objectMapper.writeValueAsString(answers);
    } catch (JsonProcessingException e) {
      logger.error("JSON writing error", e);
    }
    return answerSetJson;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String answers) {
    Map<String, Object> answerSet = null;
    try {
      answerSet = objectMapper.readValue(answers, Map.class);
    } catch (JsonParseException e) {
      logger.error("Unable to parse JSON", e);
    } catch (JsonMappingException e) {
      logger.error("Unable to map JSON", e);
    } catch (IOException e) {
      logger.error("JSON read error", e);
    }
    return answerSet;
  }
}
