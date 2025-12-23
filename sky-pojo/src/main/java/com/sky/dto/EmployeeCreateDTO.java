package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class EmployeeCreateDTO implements Serializable {
    private String username;
    private String name;
    private String phone;

    @Pattern(regexp = "^[01]$", message = "sex 只能是 0(女) 或 1(男)")
    private String sex;
    private String idNumber;
}