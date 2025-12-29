package com.sky.dto.employee;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class EmployeeCreateDTO implements Serializable {
    private String username;
    private String name;
    private String phone;

    @Pattern(regexp = "^[01]$", message = ValidationMessageConstant.SEX_INVALID)
    private String sex;
    private String idNumber;
}
