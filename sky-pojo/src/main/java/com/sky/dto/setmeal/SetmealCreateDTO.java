package com.sky.dto.setmeal;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 套餐名称 */
    private String name;

    /** 套餐价格 */
    private BigDecimal price;

    // 注意：status 字段已移除，新建套餐默认停售，由后端控制

    /** 描述信息 */
    private String description;

    /** 图片 */
    private String image;

    /** 套餐菜品关系 */
    private List<SetmealDishDTO> setmealDishes;
}
