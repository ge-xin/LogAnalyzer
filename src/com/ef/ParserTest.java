package com.ef;

import static org.junit.Assert.*;

import com.ef.db.DBConnection;
import com.ef.db.mySql.MySQLConnection;
import java.util.List;
import org.junit.Test;

public class ParserTest {

  @Test
  public void testHourly() {
    String[] args = {"--startDate=2017-01-01.15:00:00", "--duration=hourly", "--threshold=200"};
    Parser.main(args);
    DBConnection conn = MySQLConnection.getInstance();
    if (conn.shouldVisit("192.168.11.231") == null) {
      fail("Allow 192.168.11.231 to visit");
    }
    if (conn.shouldVisit("192.168.106.134") == null) {
      fail("Allow 192.168.106.134 to visit");
    }
  }

  @Test
  public void testDaily() {
    String[] args = {"--startDate=2017-01-01.00:00:00", "--duration=daily", "--threshold=500"};
    Parser.main(args);
    DBConnection conn = MySQLConnection.getInstance();
    if (conn.shouldVisit("192.168.102.136") == null) {
      fail("Allow 192.168.102.136 to visit");
    }
  }
}