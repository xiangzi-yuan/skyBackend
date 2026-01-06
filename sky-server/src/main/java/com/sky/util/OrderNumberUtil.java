package com.sky.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderNumberUtil {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    // 同一毫秒内的自增序号（0~999）
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static volatile String lastTs = "";

    private OrderNumberUtil() {}

    /**
     * 示例：OD2026010623595999912304587
     * 结构：OD + yyyyMMddHHmmssSSS + 3位序号 + 4位随机
     */
    public static String nextOrderNumber() {
        String ts = LocalDateTime.now().format(TS_FMT);

        int seq;
        if (!ts.equals(lastTs)) {
            // 时间戳变化，重置序号
            lastTs = ts;
            SEQ.set(0);
            seq = 0;
        } else {
            seq = SEQ.incrementAndGet() % 1000;
        }

        int rnd = ThreadLocalRandom.current().nextInt(10000); // 0~9999

        return "OD" + ts + String.format("%03d%04d", seq, rnd);
    }
}
