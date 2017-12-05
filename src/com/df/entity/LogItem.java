package com.df.entity;

import java.sql.Timestamp;

public class LogItem {

  private Timestamp time;
  private String ip;
  private String request;
  private int statusCode;
  private String userAgent;

  public String getIp() {
    return ip;
  }

  public Timestamp getTimestamp() {
    return time;
  }

  public String getRequest() {
    return request;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getUserAgent() {
    return userAgent;
  }


  private LogItem(LogItemBuilder builder) {
    this.time = builder.time;
    this.ip = builder.ip;
    this.request = builder.request;
    this.statusCode = builder.statusCode;
    this.userAgent = builder.userAgent;
  }

  public static class LogItemBuilder {

    private Timestamp time;
    private String ip;
    private String request;
    private int statusCode;
    private String userAgent;

    public LogItem build() {
      return new LogItem(this);
    }

    public LogItemBuilder setTimestamp(Timestamp time) {
      this.time = time;
      return this;
    }

    public LogItemBuilder setIp(String ip) {
      this.ip = ip;
      return this;
    }

    public LogItemBuilder setRequest(String request) {
      this.request = request;
      return this;
    }

    public LogItemBuilder setStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public LogItemBuilder setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
  }
}
