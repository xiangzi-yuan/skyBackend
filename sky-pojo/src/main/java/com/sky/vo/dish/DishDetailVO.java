package com.sky.vo.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品详情/编辑回显 VO
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id</li>
 *   <li>必须包含：编辑保存需要的字段（categoryId等）</li>
 *   <li>必须包含：需要回显的关联数据（flavors）</li>
 *   <li>可包含：categoryName（便于展示）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 菜品ID（必须） */
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 分类ID（编辑回显必需） */
    private Long categoryId;

    /** 分类名称（便于展示） */
    private String categoryName;

    /** 菜品价格 */
    private BigDecimal price;

    /** 图片 */
    private String image;

    /** 描述信息 */
    private String description;

    // 注意：已移除status（由单独的启售/停售接口管理）和updateTime

    /** 口味列表（编辑回显必需） */
    private List<DishFlavorVO> flavors;
}
