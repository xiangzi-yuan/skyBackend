package com.sky.dto;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    //员工id
    private Long empId;

    //旧密码
    @NotBlank(message = ValidationMessageConstant.OLD_PASSWORD_REQUIRED)
    private String oldPassword;

    //新密码
    @NotBlank(message = ValidationMessageConstant.NEW_PASSWORD_REQUIRED)
    private String newPassword;

}
