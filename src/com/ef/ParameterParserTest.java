package com.ef;

import static org.junit.Assert.*;

import com.ef.ParameterParser.Parameters;
import java.sql.Timestamp;
import org.junit.Test;

public class ParameterParserTest {

  @Test
  public void parserTest() {
    String[] args = {"--startDate=2017-01-01.13:00:00", "--duration=daily", "--threshold=250"};
    Parameters parameters =  ParameterParser.parse(args);
    Timestamp expectedTime = new Timestamp(1483304400000l);
    if (!parameters.getStartDate().equals(expectedTime)) {
      fail("wrong time");
    }
    if (parameters.getLogPath() != null) {
      fail("wrong logpath");
    }
    if (!parameters.getDuration().equals("DAY")) {
      fail("wrong duration");
    }
    if (parameters.getThreshold() != 250) {
      fail("wrong threshold");
    }
  }
}