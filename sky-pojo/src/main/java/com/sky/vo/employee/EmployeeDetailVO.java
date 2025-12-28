package com.sky.vo.employee;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工ID（必须） */
    private Long id;

    /** 账号 */
    private String username;

    /** 员工姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 性别：0-女 1-男 */
    private String sex;

    /** 身份证号 */
    private String idNumber;

    // 注意：已移除createTime/updateTime，编辑回显无需这些字段
}