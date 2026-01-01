package com.sky.readmodel.setmeal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 套餐列表查询结果（User端）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetmealListRM {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String image;
}
