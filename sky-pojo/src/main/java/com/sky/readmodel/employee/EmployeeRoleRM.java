package com.sky.readmodel.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 员工权限信息读模型（用于权限提升功能）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRoleRM {

    /** 员工ID */
    private Long id;

    /** 员工姓名 */
    private String name;

    /** 账号 */
    private String username;

    /** 当前权限等级 */
    private Integer role;
}
