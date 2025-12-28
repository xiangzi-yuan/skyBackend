package com.sky.readmodel.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品列表页 ReadModel - 用于数据库查询结果映射
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id（行操作必需）</li>
 *   <li>只包含：列表页展示的字段</li>
 *   <li>必须不包含：编辑回显/详情才需要的字段（如description、flavors）</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishPageRM {

    /** 菜品ID（必须，用于行操作） */
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 图片 */
    private String image;

    /** 分类名称（通过 JOIN 获取） */
    private String categoryName;

    /** 菜品价格 */
    private BigDecimal price;

    /** 状态：0-停售 1-起售 */
    private Integer status;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

    // 注意：已移除categoryId，列表页只需展示categoryName
    // 不包含：description、flavors（编辑回显才需要）
}
