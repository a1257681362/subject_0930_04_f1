package com.evops.service.support;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/** 业务单号生成：前缀 + 时间戳 + 进程内自增序列，配合数据库唯一约束兜底 */
@Component
public class NoGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final AtomicLong sequence = new AtomicLong(0);

    public String next(String prefix) {
        long seq = sequence.incrementAndGet() % 100000;
        return prefix + LocalDateTime.now().format(FMT) + String.format("%05d", seq);
    }
}
