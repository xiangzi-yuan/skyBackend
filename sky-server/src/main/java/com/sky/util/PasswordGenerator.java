package com.sky.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类 - 用于生成 BCrypt 加密后的密码
 * 运行 main 方法可以得到加密结果
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        // 使用与项目相同的 BCrypt 强度 (cost = 12)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后 (BCrypt cost=12):");
        System.out.println(encodedPassword);
        System.out.println("========================================");
        System.out.println("长度: " + encodedPassword.length());
        System.out.println();
        System.out.println("验证密码是否匹配: " + encoder.matches(rawPassword, encodedPassword));
    }
}
