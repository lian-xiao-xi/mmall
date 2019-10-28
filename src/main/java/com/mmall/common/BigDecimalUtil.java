package com.mmall.common;

import java.math.BigDecimal;

// 使这个类不能在外部被实例化，方法1 定义为抽象类
public abstract class BigDecimalUtil {
    // 使这个类不能在外部被实例化，方法2 将构造器设为 private
    /**
     * public BigDecimalUtil() {}
     */


    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        // 四舍五入 保留两位小数
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
    }
}
