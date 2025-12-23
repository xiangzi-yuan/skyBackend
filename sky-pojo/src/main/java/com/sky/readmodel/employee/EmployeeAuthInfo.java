package com.sky.readmodel.employee;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class EmployeeAuthInfo {
    private Long id;

    /** bcrypt hash，仅用于校验，不要外传、不要打印日志 */
    private String password;

    private Integer status;
    private Integer role;
    private Integer pwdChanged;
}
