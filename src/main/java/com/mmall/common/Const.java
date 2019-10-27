package com.mmall.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Const {
  public static final String CURRENT_USER = "CURRENT_USER";
  public static final String USERNAME = "USERNAME";
  public static final String EMAIL = "EMAIL";
  public interface Roles {
    int ROLE_CUSTOMER = 0; // 普通用户
    int ROLE_ADMIN = 1; // 管理员
  }
  public interface ProductListOrderBy {
    Set<String> PRICE_ASC_DESC = new HashSet<>(Arrays.asList("price_desc","price_asc"));
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
}
