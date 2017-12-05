package com.ef.db;

import com.ef.entity.BlockItem;
import com.ef.entity.LogItem;
import java.sql.Timestamp;
import java.util.List;

/**
 * Database Connection common interface, provide basic functions should have
 * Classes implemented this interface may support different databases
 */
public interface DBConnection {

  public void close();

  public void writeLog(LogItem item);

  public List<String> getIp(Timestamp time, int multiple, String interval, int threshold);

  public void writeBlockLog(List<String> ips, String reason);

  public BlockItem shouldVisit(String ip);
}
