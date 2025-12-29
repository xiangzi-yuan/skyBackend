package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.sky")
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@MapperScan("com.sky.mapper") // 只扫描 mapper 包，避免将 service 接口误当作 MyBatis mapper
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
