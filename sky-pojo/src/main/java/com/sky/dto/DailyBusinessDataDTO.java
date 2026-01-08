package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 日期维度的运营数据DTO，用于批量查询按日期分组的统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBusinessDataDTO {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 营业额（已完成订单金额）
     */
    private Double turnover;

    /**
     * 有效订单数（已完成订单数量）
     */
    private Integer validOrderCount;

    /**
     * 订单总数
     */
    private Integer totalOrderCount;

    /**
     * 新增用户数
     */
    private Integer newUsers;
}
