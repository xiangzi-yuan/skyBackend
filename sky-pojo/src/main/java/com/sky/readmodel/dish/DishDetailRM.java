package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品详情 ReadModel - 用于数据库查询结果映射
 * 
 * <p>职责：
 * <ul>
 *   <li>映射 dish 表和 category 表的 JOIN 查询结果</li>
 *   <li>仅包含单个菜品的基础信息和关联的分类名称</li>
 *   <li>不包含一对多的关联数据（如口味列表），关联数据由 Service 层单独查询组装</li>
 * </ul>
 * 
 * @author Sky外卖团队
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDetailRM {
    
    /** 菜品ID */
    private Long id;
    
    /** 菜品名称 */
    private String name;
    
    /** 分类ID */
    private Long categoryId;
    
    /** 分类名称（通过 JOIN category 表获取） */
    private String categoryName;
    
    /** 菜品价格 */
    private BigDecimal price;
    
    /** 图片路径 */
    private String image;
    
    /** 菜品描述 */
    private String description;
    
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    
    /** 最后更新时间 */
    private LocalDateTime updateTime;

    // 不包含实体类 flavors, 在service层组装
}
