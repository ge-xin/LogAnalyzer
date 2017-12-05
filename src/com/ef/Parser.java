package com.ef;

import com.ef.ParameterParser.Parameters;
import com.ef.db.DBConnection;
import com.ef.db.mySql.MySQLConnection;
import com.ef.entity.LogItem;
import com.ef.entity.LogItem.LogItemBuilder;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import java.util.Date;

/**
 * Parse log and directly write it to the Database using DBConnection Interface
 */
class LogParser {

  private String logPath;

  /**
   *
   * @param logPath set the log file's absolute path
   */
  public LogParser(String logPath) {
    this.logPath = logPath;
  }

  private static final String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LOG_TIME_FORMAT);

  /**
   * parse the log file and write to database
   * @throws IOException if the file does not exist
   */
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

/**
 * Parse the parameter through command line tool
 */
class ParameterParser {

  private static CommandLineParser parser = new DefaultParser();
  private static Options options;
  private static CommandLine cmd;

  /**
   * init options for only one time(Singleton)
   */
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

  /**
   * as wrapper class for parse result
   */
  public static class Parameters {

    private String logPath;
    private Timestamp startDate;
    private String duration;
    private int threshold;

    /**
     * builder pattern for future parameter change
     */
    public static class ParameterBuilder {

      private String logPath = null;
      private Timestamp startDate;
      private String duration;
      private int threshold;

      public Parameters build() {
        return new Parameters(this);
      }

      public ParameterBuilder set(String logPath, Timestamp startDate, String duration,
          int threshold) {
        this.logPath = logPath;
        this.startDate = startDate;
        this.duration = duration;
        this.threshold = threshold;
        return this;
      }
    }

    private Parameters(ParameterBuilder builder) {
      this.logPath = builder.logPath;
      this.startDate = builder.startDate;
      this.duration = builder.duration;
      this.threshold = builder.threshold;
    }

    public String getLogPath() {
      return logPath;
    }

    public Timestamp getStartDate() {
      return startDate;
    }

    public String getDuration() {
      return duration;
    }

    public int getThreshold() {
      return threshold;
    }

    public static ParameterBuilder builder() {
      return new ParameterBuilder();
    }
  }

  private static final String formatStr = "java -cp \"parser.jar\" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100";
  private static final String HOURLY = "hourly";
  private static final String DAILY = "daily";
  private static final String HOUR = "HOUR";
  private static final String DAY = "DAY";

  /**
   * parse the command line String array into Parameters wrapper class
   * @param args command line read in by Java
   * @return Parse result in Parameters instance
   */
  public static Parameters parse(String[] args) {
    // java -cp "parser.jar" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
    initOptions();
    String logPath = null;
    Timestamp startDate;
    String duration;
    int threshold;
    try {
      cmd = parser.parse(options, args);
      if (cmd.hasOption("l")) {
        logPath = cmd.getOptionValue("l");
      }
      startDate = new Timestamp(simpleDateFormat.parse(cmd.getOptionValue("s")).getTime());
      duration = cmd.getOptionValue("d").equals(HOURLY) ? HOUR : DAY;
      threshold = Integer.valueOf(cmd.getOptionValue("t"));
      return Parameters.builder().set(logPath, startDate, duration, threshold).build();
    } catch (org.apache.commons.cli.ParseException | java.text.ParseException e) {
      System.out.println(formatStr);
      e.printStackTrace();
    }
    return null;
  }
}

public class Parser {

  private static final String HOUR = "HOUR";
  private static final String DAY = "DAY";
  private static final String BLOCK_REASON_HOUR = "Too many visit in a hour";
  private static final String BLOCK_REASON_DAY = "Too many visit in a 24 hours";

  public static void main(String[] args) {
    // Parameter parsing
    Parameters parameters = ParameterParser.parse(args);
    if (parameters == null) {
      return;
    }
    // if new log file was given, parse it
    if (parameters.getLogPath() != null) {
      LogParser logParser = new LogParser(parameters.getLogPath());
      try {
        logParser.parse();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // connect db do query and write block ip back to database
    DBConnection conn = MySQLConnection.getInstance();
    List<String> shouldBlockIps = conn
        .getIp(parameters.getStartDate(), 1, parameters.getDuration(), parameters.getThreshold());
    String reason;
    if (parameters.getDuration().equals(HOUR)) {
      reason = BLOCK_REASON_HOUR;
    } else {
      reason = BLOCK_REASON_DAY;
    }
    conn.writeBlockLog(shouldBlockIps, reason);
  }
}
