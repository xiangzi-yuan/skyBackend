package com.sky.vo.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分类简略信息 VO - 用于下拉选择等场景
 * 
 * <p>例如：菜品/套餐新增时选择分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 类型：1-菜品分类 2-套餐分类
     */
    private Integer type;
}
