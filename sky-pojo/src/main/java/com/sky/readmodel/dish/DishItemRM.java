package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 套餐包含的菜品项 ReadModel
 * 用于 User 端展示套餐内的菜品信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishItemRM implements Serializable {

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 份数
     */
    private Integer copies;

    /**
     * 菜品图片
     */
    private String image;

    /**
     * 菜品描述
     */
    private String description;
}
