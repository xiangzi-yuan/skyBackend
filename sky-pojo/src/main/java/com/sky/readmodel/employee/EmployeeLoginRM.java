package com.sky.readmodel.employee;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class EmployeeLoginRM {
    private Long id;
    private String username;
    private String name;

    /** bcrypt hash，仅用于 Service 校验，不要外传、不要打印日志 */
    private String password;

    private Integer status;
    private Integer role;
    private Integer pwdChanged;
}
