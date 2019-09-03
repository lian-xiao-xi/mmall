package com.mmall.util;

import java.security.MessageDigest;

public class MD5Util {
  private static String byteArrayToHexString(byte b[]) {
//    StringBuffer resultSb = new StringBuffer();
    // StringBuilder 和 StringBuffer 之间的最大不同在于 StringBuilder 的方法不是线程安全的（不能同步访问）,StringBuilder 相较于 StringBuffer 有速度优势
    StringBuilder resultSb = new StringBuilder();
    for(byte by: b) {
      resultSb.append(byteToHexString(by));
    }
//    for (int i = 0; i < b.length; i++)
//      resultSb.append(byteToHexString(b[i]));
    
    return resultSb.toString();
  }
  
  private static String byteToHexString(byte b) {
    int n = b;
    if (n < 0)
      n += 256;
    int d1 = n / 16;
    int d2 = n % 16;
    return hexDigits[d1] + hexDigits[d2];
  }
  
  /**
   * 返回大写MD5
   *
   * @param origin
   * @param charsetname
   * @return
   */
  private static String MD5Encode(String origin, String charsetname) {
    String resultString = null;
    try {
      resultString = new String(origin);
      MessageDigest md = MessageDigest.getInstance("MD5");
      if (charsetname == null || "".equals(charsetname))
        resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
      else
        resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
    } catch (Exception exception) {
    }
    return resultString.toUpperCase();
  }
  
  public static String MD5EncodeUtf8(String origin) {
    origin = origin + PropertiesUtil.getProperty("password.salt", "");
    return MD5Encode(origin, "utf-8");
  }
  
  
  private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
    "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
  
}
