package com.sky.vo.employee;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EmployeePageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工ID（必须，用于行操作） */
    private Long id;

    /** 账号 */
    private String username;

    /** 员工姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 账号状态：0-禁用 1-启用 */
    private Integer status;

    /** 最后操作时间（列表显示） */
    private LocalDateTime updateTime;

    // 注意：已移除createTime，前端列表只显示“最后操作时间”
}
