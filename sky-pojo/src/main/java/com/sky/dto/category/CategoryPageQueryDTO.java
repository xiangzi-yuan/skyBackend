package com.sky.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询 DTO
 * 
 * <p>用于分页查询的条件参数
 */
@Data
public class CategoryPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 每页记录数
     */
    private int pageSize = 10;

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 分类类型：1-菜品分类 2-套餐分类
     */
    private Integer type;
}
