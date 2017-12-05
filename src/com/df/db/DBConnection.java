package com.df.db;

import com.df.entity.LogItem;
import java.sql.Timestamp;
import java.util.List;

public interface DBConnection {

  public void close();

  public void writeLog(LogItem item);

  public List<String> getIp(Timestamp time, String duration);

  public void writeBlockLog(String ip, String reason);
}
