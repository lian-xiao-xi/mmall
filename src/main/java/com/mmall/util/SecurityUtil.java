package com.mmall.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

public class SecurityUtil {
  // 返回大写MD5
  public static String MD5Encode(String str, String code) {
    // StringBuilder 和 StringBuffer 之间的最大不同在于 StringBuilder 的方法不是线程安全的（不能同步访问）,StringBuilder 相较于 StringBuffer 有速度优势
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] bytes = md.digest(str.getBytes(code));
      char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
      StringBuilder ret = new StringBuilder(bytes.length * 2);

      for(int i = 0; i < bytes.length; ++i) {
        ret.append(HEX_DIGITS[bytes[i] >> 4 & 15]);
        ret.append(HEX_DIGITS[bytes[i] & 15]);
      }

      return ret.toString();
    } catch (Exception var7) {
      throw new RuntimeException(var7);
    }
  }

  public static String MD5EncodeUtf8(String str) {
    return MD5Encode(str, "utf-8");
  }

  public static String sha256HMAC(String message, String secret) {
    return sha256HMAC(message, secret, "utf-8");
  }

  public static String sha256HMAC(String message, String secret, String code) {
    try {
      byte[] value = message.getBytes(code);
      byte[] key = secret.getBytes(code);
      SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);
      byte[] b = mac.doFinal(value);
      StringBuilder hs = new StringBuilder();

      for(int n = 0; b != null && n < b.length; ++n) {
        String stmp = Integer.toHexString(b[n] & 255);
        if (stmp.length() == 1) {
          hs.append('0');
        }

        hs.append(stmp);
      }

      return hs.toString().toUpperCase();
    } catch (Exception var11) {
      var11.printStackTrace();
      return "";
    }
  }
}
