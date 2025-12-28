package com.sky.vo.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品-套餐关联VO（用于删除时的错误提示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSetmealRelationVO {
    /**
     * 菜品ID（用于分组，避免同名菜品被错误合并）
     */
    private Long dishId;
    
    /**
     * 菜品名称
     */
    private String dishName;
    
    /**
     * 套餐名称
     */
    private String setmealName;
}
