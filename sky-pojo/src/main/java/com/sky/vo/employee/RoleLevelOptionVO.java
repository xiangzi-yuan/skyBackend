package com.sky.vo.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权限等级选项VO（用于前端下拉框）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleLevelOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 权限等级值：1-普通员工 5-经理 9-老板 */
    private Integer value;

    /** 权限等级名称：STAFF/MANAGER/SUPER */
    private String label;

    /** 权限等级中文描述 */
    private String description;
}
