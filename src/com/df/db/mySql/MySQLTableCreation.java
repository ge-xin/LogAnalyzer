package com.df.db.mySql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {

  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection conn = null;
    try {
      System.out.println("Connecting to \n" + MySQLDBUtil.URL);
      conn = DriverManager.getConnection(MySQLDBUtil.URL);
    } catch (SQLException e) {
      System.out.println("SQLException " + e.getMessage());
      System.out.println("SQLState " + e.getSQLState());
      System.out.println("VendorError" + e.getErrorCode());
    }
    if (conn == null) {
      return;
    }

    Statement stmt = conn.createStatement();

    String sql = "DROP TABLE IF EXISTS log";
    stmt.executeUpdate(sql);

    sql = "DROP TABLE IF EXISTS block";
    stmt.executeUpdate(sql);

    System.out.println("Drop is done successfully.");

    // Step 2. Create new tables.
    sql = "CREATE TABLE log " + "(ip VARCHAR(45) NOT NULL, " + " date TIMESTAMP(3), "
        + "request VARCHAR(255), " + "status SMALLINT," + "user_agent VARCHAR(255),"
        + " PRIMARY KEY (ip, date))";
    stmt.executeUpdate(sql);

    sql =
        "CREATE TABLE block " + "(ip VARCHAR(45) NOT NULL, " + " reason VARCHAR(255), "
            + " PRIMARY KEY (ip))";
    stmt.executeUpdate(sql);

    System.out.println("Import is done successfully.");
  }
}
