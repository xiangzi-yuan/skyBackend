package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 菜品详情/编辑回显 ReadModel - 用于数据库查询结果映射
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id</li>
 *   <li>必须包含：编辑保存所需的关键业务字段（如 categoryId）</li>
 *   <li>可包含：页面展示需要的派生字段（如 categoryName）</li>
 *   <li>不包含：一对多的关联数据（如口味列表），由 Service 层单独查询组装</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDetailRM {
    
    /** 菜品ID（必须） */
    private Long id;
    
    /** 菜品名称 */
    private String name;
    
    /** 分类ID（编辑回显必需） */
    private Long categoryId;
    
    /** 分类名称（通过 JOIN category 表获取，便于展示） */
    private String categoryName;
    
    /** 菜品价格 */
    private BigDecimal price;
    
    /** 图片路径 */
    private String image;
    
    /** 菜品描述 */
    private String description;

    /** 状态：0-停售 1-启售（用于业务校验，如启售状态不能删除） */
    private Integer status;

    // 不包含 flavors，由 Service 层单独查询组装
}
