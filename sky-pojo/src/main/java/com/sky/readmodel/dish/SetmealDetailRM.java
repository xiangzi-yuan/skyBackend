package com.sky.readmodel.dish;

import com.sky.dto.SetmealDishDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetmealDetailRM {
    private Long id;
    //分类
    private Long categoryId;
    private String categoryName;
    //套餐名称
    private String name;
    //套餐价格
    private BigDecimal price;
    //状态 0:停用 1:启用
    private Integer status;
    //描述信息
    private String description;
    //图片
    private String image;
    // 不含套餐菜品关系
}

