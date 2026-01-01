package com.sky.dto.employee;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 员工权限提升DTO
 */
@Data
public class EmployeeRoleUpgradeDTO implements Serializable {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotNull(message = "新权限等级不能为空")
    @Min(value = 1, message = "权限等级最小为1")
    @Max(value = 9, message = "权限等级最大为9")
    private Integer newRole;
}
