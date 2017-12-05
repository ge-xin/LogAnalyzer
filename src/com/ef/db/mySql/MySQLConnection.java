package com.ef.db.mySql;

import com.ef.db.DBConnection;
import com.ef.entity.BlockItem;
import com.ef.entity.LogItem;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * Provide database operation functions in MySQL
 */
public class MySQLConnection implements DBConnection {

  private static MySQLConnection instance;

  /**
   * get connection instance
   * @return the singleton
   */
  public static MySQLConnection getInstance() {
    if (instance == null) {
      instance = new MySQLConnection();
    }
    return instance;
  }

  private Connection conn = null;

  private MySQLConnection() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(MySQLDBUtil.URL);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    if (conn == null) {
      return;
    }
    try {
      conn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final String INSERT_INTO_LOG_VALUES = "INSERT INTO `log` VALUES (?, ?, ?, ?, ?)";

  /**
   * write the log item(row) in to database
   * @param logItem a parsed log row
   */
  @Override
  public void writeLog(LogItem logItem) {
    try {
      PreparedStatement statement = conn.prepareStatement(INSERT_INTO_LOG_VALUES);
      statement.setString(1, logItem.getIp());
      statement.setTimestamp(2, logItem.getTimestamp());
      statement.setString(3, logItem.getRequest());
      statement.setInt(4, logItem.getStatusCode());
      statement.setString(5, logItem.getUserAgent());
      statement.executeUpdate();
    } catch (SQLIntegrityConstraintViolationException e){
      // do nothing here
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static final String INSERT_INTO_BLOCK_VALUES = "INSERT INTO `block` VALUES (?, ?);";

  /**
   * record the blocked ips
   * @param ips a list of ips in String format
   * @param reason the reason why it is blocked
   */
  @Override
  public void writeBlockLog(List<String> ips, String reason) {
    for (String ip : ips) {
      try {
        PreparedStatement statement = conn.prepareStatement(INSERT_INTO_BLOCK_VALUES);
        statement.setString(1, ip);
        statement.setString(2, reason);
        statement.executeUpdate();
      } catch (SQLIntegrityConstraintViolationException e) {
        // do nothing here
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private static final String GET_IP_QUERY_HOUR = "SELECT `ip`, COUNT(`date`) FROM ("
      + " SELECT `ip`, `date` FROM `log`WHERE `date` >= ? and `date` <= DATE_ADD(?, INTERVAL ? HOUR)"
      + ")visit GROUP BY `ip` HAVING COUNT(`date`) > ?;";

  private static final String GET_IP_QUERY_DAY = "SELECT `ip`, COUNT(`date`) FROM ("
      + " SELECT `ip`, `date` FROM `log`WHERE `date` >= ? and `date` <= DATE_ADD(?, INTERVAL ? DAY)"
      + ")visit GROUP BY `ip` HAVING COUNT(`date`) > ?;";

  /**
   * Get a list of ips should be blocked
   * @param timestamp started time
   * @param multiple the number of intervals
   * @param interval the kind of intervals
   * @param threshold threshold of times of visits
   * @return
   */
  @Override
  public List<String> getIp(Timestamp timestamp, int multiple, String interval, int threshold) {
    List<String> shouldBlockIps = new ArrayList<>();
    PreparedStatement statement = null;
    try {
      if (interval.equals("HOUR")) {
        statement = conn.prepareStatement(GET_IP_QUERY_HOUR);
      } else {
        statement = conn.prepareStatement(GET_IP_QUERY_DAY);
      }
      statement.setTimestamp(1, timestamp);
      statement.setTimestamp(2, timestamp);
      statement.setInt(3, multiple);
      statement.setInt(4, threshold);
      ResultSet re = statement.executeQuery();
      while (re.next()) {
        shouldBlockIps.add(re.getString("ip"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return shouldBlockIps;
  }

  private static final String SHOULD_VISIT_QUERY = "SELECT `ip` FROM `block` where `ip` =  ?;";

  /**
   * Check if the ip is allowed to visit
   * @param ip ip of visitor
   * @return true if current visit is allowed
   */
  @Override
  public BlockItem shouldVisit(String ip) {
    try {
      PreparedStatement statement = conn.prepareStatement(SHOULD_VISIT_QUERY);
      statement.setString(1, ip);
      ResultSet re = statement.executeQuery();
      if (re.next()) {
        return new BlockItem.BlockItemBuilder().set(re.getString("ip"), re.getString("reason"))
            .build();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
