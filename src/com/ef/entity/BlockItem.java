package com.ef.entity;

/**
 * the model of a row in block ip list in database
 */
public class BlockItem {

  private String ip;
  private String reason;

  private BlockItem(BlockItemBuilder builder) {
    this.ip = builder.ip;
    this.reason = builder.reason;
  }

  public static class BlockItemBuilder {

    private String ip;
    private String reason;

    public BlockItem build() {
      return new BlockItem(this);
    }

    public BlockItemBuilder set(String ip, String reason) {
      this.ip = ip;
      this.reason = reason;
      return this;
    }
  }

  public String getIp() {
    return ip;
  }

  public String getReason() {
    return reason;
  }
}
