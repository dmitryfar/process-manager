package org.activiti.pm.util;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateFromStringDeserializer extends JsonDeserializer<Date> {
  private static Logger logger = LoggerFactory.getLogger(DateFromStringDeserializer.class);

  protected DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

  @Override
  public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String tmpDate = jp.getText();
    if (tmpDate != null) {
      DateTime date = dateTimeFormatter.parseDateTime(tmpDate);
      return date.toDate();
    } else {
      return null;
    }
  }
}
