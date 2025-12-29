package com.sky.dto.employee;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import com.sky.constant.ValidationMessageConstant;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    @NotBlank(message = ValidationMessageConstant.USERNAME_REQUIRED)
    private String username;

    @ApiModelProperty("密码")
    @NotBlank(message = ValidationMessageConstant.PASSWORD_REQUIRED)
    private String password;

}
