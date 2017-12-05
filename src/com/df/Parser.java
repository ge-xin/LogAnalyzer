package com.df;

import com.df.db.DBConnection;
import com.df.db.mySql.MySQLConnection;
import com.df.entity.LogItem;
import com.df.entity.LogItem.LogItemBuilder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.util.Date;

class LogParser {

  private String logPath;

  public LogParser(String logPath) {
    this.logPath = logPath;
  }

  private static final String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LOG_TIME_FORMAT);

  public void parse() throws IOException {
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(logPath));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (in == null) {
      return;
    }
    try {
      String line;
      while ((line = in.readLine()) != null) {
        String[] row = line.split("\\|");
        DBConnection conn = MySQLConnection.getInstance();
        LogItem curItem = new LogItemBuilder()
            .setTimestamp(new Timestamp(simpleDateFormat.parse(row[0]).getTime()))
            .setIp(row[1])
            .setRequest(row[2])
            .setStatusCode(Integer.valueOf(row[3]))
            .setUserAgent(row[4]).build();
        conn.writeLog(curItem);
      }
    } catch (IOException | java.text.ParseException e) {
      e.printStackTrace();
    } finally {
      in.close();
    }
  }
}

public class Parser {

  private static CommandLineParser parser = new DefaultParser();
  private static Options options;
  private static CommandLine cmd;

  private static void initOptions() {
    if (options != null) {
      return;
    }
    options = new Options();
    options.addOption(Option.builder("l")
        .hasArg()
        .longOpt("accesslog")
        .optionalArg(true)
        .type(String.class)
        .valueSeparator()
        .build());
    options.addOption(Option.builder("s")
        .desc("Start date in yyyy-MM-dd HH:mm:ss.SSS format")
        .hasArg()
        .longOpt("startDate")
        .required()
        .type(Date.class)
        .valueSeparator().build());
    options.addOption(Option.builder("d")
        .desc("Duration of the query(hourly/daily)")
        .hasArg()
        .longOpt("duration")
        .required()
        .type(String.class)
        .valueSeparator().build());
    options.addOption(Option.builder("t")
        .desc("Threshold of blocking certain ips")
        .hasArg()
        .longOpt("threshold")
        .required()
        .type(Integer.class)
        .valueSeparator().build());
  }

  private static final String LOG_TIME_FORMAT = "yyyy-M-d.H:m:s";
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LOG_TIME_FORMAT);

  public static void main(String[] args) {

    // java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
    initOptions();
    String formatStr = "java -cp \"parser.jar\" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100";
    String logPath = null;
    Timestamp startDate;
    String duration;
    Integer threshold;

    try {
      cmd = parser.parse(options, args);
      if (cmd.hasOption("l")) {
        logPath = cmd.getOptionValue("l");
      }
      startDate = new Timestamp(simpleDateFormat.parse(cmd.getOptionValue("s")).getTime());
      duration = cmd.getOptionValue("d");
      threshold = Integer.valueOf(cmd.getOptionValue("t"));
      System.out.println(logPath);
      System.out.println(startDate);
      System.out.println(duration);
      System.out.println(threshold);

      if (logPath != null) {
        try {
          new LogParser(logPath).parse();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      DBConnection conn = MySQLConnection.getInstance();
      List<String> res = conn.getIp(startDate, "monthly");
      System.out.println(res);
    } catch (ParseException e) {
      System.out.println(formatStr);
      e.printStackTrace();
    } catch (java.text.ParseException e) {
      e.printStackTrace();
    }

    System.out.println();
  }
}
