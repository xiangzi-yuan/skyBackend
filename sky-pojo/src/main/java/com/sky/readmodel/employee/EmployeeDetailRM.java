package com.sky.readmodel.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDetailRM {

    /** 员工ID（必须） */
    private Long id;

    /** 员工姓名 */
    private String name;

    /** 账号 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 性别：0-女 1-男 */
    private String sex;

    /** 身份证号 */
    private String idNumber;

    /** 状态：0-禁用 1-启用（用于业务校验） */
    private Integer status;
}
