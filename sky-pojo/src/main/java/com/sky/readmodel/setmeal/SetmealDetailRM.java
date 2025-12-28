package com.sky.readmodel.setmeal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetmealDetailRM {

    /** 套餐ID（必须） */
    private Long id;

    /** 分类ID（编辑回显必需） */
    private Long categoryId;

    /** 套餐名称 */
    private String name;

    /** 套餐价格 */
    private BigDecimal price;

    /** 描述信息 */
    private String description;

    /** 图片 */
    private String image;

    /** 状态：0-停售 1-启售（用于业务校验，如启售状态不能删除） */
    private Integer status;

    // 不含套餐菜品关系，由 Service 层组装
}

