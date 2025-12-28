package com.sky.readmodel.dish;

import com.sky.vo.SetmealDishVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetmealPageRM {
    //套餐id
    private Long id;
    //分类id
    private Long categoryId;
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
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
}
