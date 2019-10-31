package com.mmall.common;

public enum ResponseCode {
  SUCCESS(0, "SUCCESS"),
  ERROR(1, "ERROR"),
  NEED_LOGIN(10, "NEED_LOGIN"),
  ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT"),
  // 商品添加购物车或更新购物车商品数量时商品库存不足
  INVENTORY_SHORTAGE(3, "库存不足");

  private final int code;
  private final String desc;

  ResponseCode(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }
}
