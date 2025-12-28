package com.sky.dto.setmeal;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套餐ID（必须） */
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 套餐名称 */
    private String name;

    /** 套餐价格 */
    private BigDecimal price;

    // 注意：status 字段已移除，状态由单独的启售/停售接口管理

    /** 描述信息 */
    private String description;

    /** 图片 */
    private String image;

    /** 套餐菜品关系 */
    private List<SetmealDishDTO> setmealDishes;
}
