package com.sky.vo.setmeal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐详情/编辑回显 VO
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id</li>
 *   <li>必须包含：编辑保存需要的字段（categoryId等）</li>
 *   <li>必须包含：需要回显的关联数据（setmealDishes）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    // 注意：已移除status（由单独的启售/停售接口管理）和updateTime

    /** 套餐菜品关联关系（编辑回显必需） */
    @Builder.Default
    private List<SetmealDishVO> setmealDishes = new ArrayList<>();
}
