package com.sky.dto.employee;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EmployeeUpdateDTO implements Serializable {
    @NotNull(message = ValidationMessageConstant.ID_REQUIRED)
    private Long id; // id
    private String name; // 姓名
    private String phone;
    private String sex;
    private String idNumber; // 身份证
    private String username; // 账号
}
