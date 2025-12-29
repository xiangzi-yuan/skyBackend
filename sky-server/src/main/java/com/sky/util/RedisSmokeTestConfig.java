package com.sky.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("dev")
@Slf4j
public class RedisSmokeTestConfig {

    @Bean
    public CommandLineRunner redisSmokeTest(StringRedisTemplate template) {
        return args -> {
            String key = "smoke:k1";
            template.opsForValue().set(key, "v1");
            String v = template.opsForValue().get(key);
            log.info("Redis Test : GET => " + v);
            template.delete(key);
        };
    }
}
