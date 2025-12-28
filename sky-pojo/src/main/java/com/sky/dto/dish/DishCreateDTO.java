package com.sky.dto.dish;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜品名称 */
    private String name;

    /** 菜品分类ID */
    private Long categoryId;

    /** 菜品价格 */
    private BigDecimal price;

    /** 图片 */
    private String image;

    /** 描述信息 */
    private String description;

    // 注意：status 字段已移除，新建菜品默认停售，由后端控制

    /** 口味列表 */
    private List<DishFlavorDTO> flavors;
}
