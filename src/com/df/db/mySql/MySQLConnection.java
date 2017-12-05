package com.df.db.mySql;

import com.df.db.DBConnection;
import com.df.entity.LogItem;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class MySQLConnection implements DBConnection {

  private static MySQLConnection instance;

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

  private static final String insertLogItemQuery = "INSERT INTO `log` VALUES (?, ?, ?, ?, ?)";

  @Override
  public void writeLog(LogItem logItem) {
    try {
      PreparedStatement statement = conn.prepareStatement(insertLogItemQuery);
      statement.setString(1, logItem.getIp());
      statement.setTimestamp(2, logItem.getTimestamp());
      statement.setString(3, logItem.getRequest());
      statement.setInt(4, logItem.getStatusCode());
      statement.setString(5, logItem.getUserAgent());
      System.out.println(statement.toString());
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static final String insertBlockItemQuery = "INSERT INTO `block` VALUES (?, ?);";

  @Override
  public void writeBlockLog(String ip, String reason) {
    try {
      PreparedStatement statement = conn.prepareStatement(insertBlockItemQuery);
      statement.setString(1, ip);
      statement.setString(2, reason);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static final String getIpQuery = "SELECT `ip`, COUNT(`date`) FROM ("
      + " SELECT `ip`, `date` FROM `log`WHERE `date` >= ? and `date` <= DATE_ADD(?, INTERVAL 6 HOUR)"
      + ")visit GROUP BY `ip` HAVING COUNT(`date`) >= 100 ORDER BY COUNT(`date`) DESC;";

  @Override
  public List<String> getIp(Timestamp timestamp, String duration) {
    List<String> shouldBlockIps = new ArrayList<>();
    try {
      PreparedStatement statement = conn.prepareStatement(getIpQuery);
      statement.setTimestamp(1, timestamp);
      statement.setTimestamp(2, timestamp);
      ResultSet re = statement.executeQuery();
      while (re.next()) {
        shouldBlockIps.add(re.getString("ip"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return shouldBlockIps;
  }
}
