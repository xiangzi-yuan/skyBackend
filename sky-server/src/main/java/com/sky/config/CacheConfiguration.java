package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 * 
 * <p>
 * 策略说明：
 * <ul>
 * <li>列表类缓存（如 setmealCache）：设置较短 TTL（10分钟），允许短期不一致</li>
 * <li>更新时只清除关键缓存，配合 TTL 保证最终一致性</li>
 * </ul>
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfiguration {

    /**
     * 配置 Redis 缓存管理器，支持不同缓存的 TTL 策略
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("初始化 Redis 缓存管理器...");

        // 默认缓存配置：30分钟过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 为不同缓存配置不同的 TTL
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // setmealCache：10分钟过期（列表类缓存，允许短期不一致）
        cacheConfigurations.put("setmealCache", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // dishCache：10分钟过期（如果后续需要）
        cacheConfigurations.put("dishCache", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // categoryCache：30分钟过期（分类变动较少）
        cacheConfigurations.put("categoryCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put("setmealDetailCache", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigurations.put("dishDetailCache", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
