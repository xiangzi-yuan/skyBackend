package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishPageRM {
    private Long id;
    //菜品名称
    private String name;
    //菜品分类id
    private Long categoryId;
    //菜品分类名称
    private String categoryName;
    //菜品价格
    private BigDecimal price;
    //图片
    private String image;
    //0 停售 1 起售
    private Integer status;
    private LocalDateTime updateTime;
    // 重点:显示在列表中的内容,一般不需要口味
}
