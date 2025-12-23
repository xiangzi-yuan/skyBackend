package com.sky.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EmployeeDetailVO implements Serializable {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}