package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeUpdateDTO implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
    // username 是否允许改，看业务决定
}
