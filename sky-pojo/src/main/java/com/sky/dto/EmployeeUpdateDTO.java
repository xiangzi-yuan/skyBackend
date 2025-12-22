package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeUpdateDTO implements Serializable {
    private Long id; // id
    private String name; // 姓名
    private String phone;
    private String sex;
    private String idNumber; // 身份证
    private String username; // 账号
}
