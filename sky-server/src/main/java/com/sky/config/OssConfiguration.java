package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建 OssUtil 工具类
 */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean(AliOssUtil.class)
    public AliOssUtil aliOssUtil(AliOssProperties props) {
        log.info("创建 AliOssUtil 对象, endpoint={}, bucket={}", props.getEndpoint(), props.getBucketName());
        return new AliOssUtil(
                props.getEndpoint(),
                props.getAccessKeyId(),
                props.getAccessKeySecret(),
                props.getBucketName()
        );
    }
}
