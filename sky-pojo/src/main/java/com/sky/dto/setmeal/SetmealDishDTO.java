package com.sky.dto.setmeal;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SetmealDishDTO {

    //菜品id
    private Long dishId;

    //菜品名称
    private String name;

    //菜品原价
    private BigDecimal price;

    //份数
    private Integer copies;
}
