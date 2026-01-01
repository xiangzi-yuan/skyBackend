package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品-套餐关联 ReadModel
 * 用于删除菜品时查询关联的套餐信息（错误提示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSetmealRelationRM implements Serializable {

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
