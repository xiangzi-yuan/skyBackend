package com.sky.readmodel.employee;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDetailRM {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String sex;
    private String idNumber;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
