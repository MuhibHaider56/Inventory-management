package com.bachat.inventory.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    private MoneyUtil() {}

    public static BigDecimal scale2(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return scale2(scale2(a).multiply(scale2(b)));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return scale2(scale2(a).subtract(scale2(b)));
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return scale2(scale2(a).add(scale2(b)));
    }
}
