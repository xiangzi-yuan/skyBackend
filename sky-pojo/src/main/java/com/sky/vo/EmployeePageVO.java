package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeePageVO {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
