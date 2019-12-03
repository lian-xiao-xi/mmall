package com.mmall.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Const {
  public static final String CURRENT_USER = "CURRENT_USER";
  public static final String CURRENT_USER_NAME = "CURRENT_USER_NAME";
  public static final String CURRENT_USER_TOKEN = "CURRENT_USER_TOKEN";
  public static final String USERNAME = "USERNAME";
  public static final String EMAIL = "EMAIL";
  public interface Roles {
    int ROLE_CUSTOMER = 0; // 普通用户
    int ROLE_ADMIN = 1; // 管理员
  }
//  alipay 异步通知交易状态说明（文档：https://docs.open.alipay.com/194/103296/）
  public enum AlipayTradeStatus {
    WAIT_BUYER_PAY, TRADE_CLOSED, TRADE_SUCCESS, TRADE_FINISHED
  }
//  接收到异步通知后返回给支付宝的字符
  public interface AlipayCallbackResponseCode {
    String RESPONSE_SUCCESS = "success";
    String RESPONSE_FAILED = "failed";
  }
  public interface CartCheckedCode {
    int IS_CHECKED = 1;
    int UN_CHECKED = 1;
  }
  public interface ProductListOrderBy {
    Set<String> PRICE_ASC_DESC = new HashSet<>(Arrays.asList("price_desc","price_asc"));
  }

  public enum OrderStatusEnum {
    CANCELED(0,"已取消"),
    NO_PAY(10,"未支付"),
    PAID(20,"已付款"),
    SHIPPED(40,"已发货"),
    ORDER_SUCCESS(50,"订单完成"),
    ORDER_CLOSE(60,"订单关闭");


    OrderStatusEnum(int code,String value){
      this.code = code;
      this.value = value;
    }
    private String value;
    private int code;

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }

    public static OrderStatusEnum codeOf(int code){
      for(OrderStatusEnum orderStatusEnum : values()){
        if(orderStatusEnum.getCode() == code){
          return orderStatusEnum;
        }
      }
      throw new RuntimeException("么有找到对应的枚举");
    }
  }

  public enum PayPlatformEnum{
    ALIPAY(1,"支付宝");

    PayPlatformEnum(int code,String value){
      this.code = code;
      this.value = value;
    }
    private String value;
    private int code;

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }
  }

  public enum ProductStatus {
    ON_SALE(1, "在线");

    private String value;
    private int code;

    ProductStatus(int code, String value) {
      this.value = value;
      this.code = code;
    }

    public String getValue() {
      return value;
    }
    public int getCode() {
      return code;
    }
  }

  public enum PaymentTypeEnum {
    ONLINE_PAY(1, "在线支付");

    private int code;
    private String value;

    PaymentTypeEnum(int code, String value) {
      this.value = value;
      this.code = code;
    }

    public String getValue() {
      return value;
    }

    public int getCode() {
      return code;
    }
  }
}
