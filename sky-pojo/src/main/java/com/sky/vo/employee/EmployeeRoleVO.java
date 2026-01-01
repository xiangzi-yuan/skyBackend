package com.sky.vo.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工权限信息VO（仅显示权限相关信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工ID */
    private Long id;

    /** 员工姓名 */
    private String name;

    /** 账号 */
    private String username;

    /** 当前权限等级：1-普通员工 5-经理 9-老板 */
    private Integer role;

    /** 权限等级名称：STAFF/MANAGER/SUPER */
    private String roleName;
}
