package com.sky.service.impl;

import com.sky.service.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
public class VersionServiceImpl implements VersionService {

    private final StringRedisTemplate stringRedisTemplate;

    public VersionServiceImpl(RedisConnectionFactory connectionFactory) {
        this.stringRedisTemplate = new StringRedisTemplate(connectionFactory);
    }

    @Override
    public long getOrInit(String verKey) {
        if (verKey == null || verKey.isBlank()) {
            return 1L;
        }

        try {
            String val = stringRedisTemplate.opsForValue().get(verKey);
            if (val != null) {
                return parseOrDefault(val, 1L);
            }

            Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(verKey, "1");
            if (Boolean.TRUE.equals(ok)) {
                return 1L;
            }

            val = stringRedisTemplate.opsForValue().get(verKey);
            return parseOrDefault(val, 1L);
        } catch (Exception e) {
            log.warn("Redis getOrInit failed, verKey={}", verKey, e);
            return 1L;
        }
    }

    @Override
    public void bump(String verKey) {
        if (verKey == null || verKey.isBlank()) {
            return;
        }

        try {
            stringRedisTemplate.opsForValue().increment(verKey);
        } catch (Exception e) {
            log.warn("Redis bump failed, verKey={}", verKey, e);
        }
    }

    @Override
    public void bumpAfterCommit(String verKey) {
        if (verKey == null || verKey.isBlank()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    bump(verKey);
                }
            });
            return;
        }

        bump(verKey);
    }

    private static long parseOrDefault(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

