package com.stockdata.demo_stock_data_app.util;

import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;


public class TimeRange {
    private static final Map<String, Pair<String, String>> VALID_COMBINATIONS = Map.of(
        "8d_1m", Pair.of("8d", "1m"),
        "60d_5m", Pair.of("60d", "5m"),
        "60d_15m", Pair.of("60d", "15m"),
        "60d_30m", Pair.of("60d", "30m"),
        "60d_90m", Pair.of("60d", "90m"),
        "730d_1h", Pair.of("730d", "1h"),
        "max_1d", Pair.of("max", "1d"),
        "max_1mo", Pair.of("max", "1mo"),
        "max_3mo", Pair.of("max", "3mo")
    );

    // 檢查 range + interval 是否合法
    public static boolean isValid(String range, String interval) {
        return VALID_COMBINATIONS.containsValue(Pair.of(range, interval));
    }

    // 也可以提供一個方法，直接用 key 檢查
    public static boolean isValidKey(String key) {
        return VALID_COMBINATIONS.containsKey(key);
    }
}
