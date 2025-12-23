package com.sky.auth.model;

import lombok.Data;

@Data
public class EmployeeAuthInfo {
    private Long id;
    private String password;   // bcrypt hash
    private Integer status;    // 是否禁用
    private Integer role;      // 权限级别
    private Integer pwdChanged;// 是否改过初始密码
}
