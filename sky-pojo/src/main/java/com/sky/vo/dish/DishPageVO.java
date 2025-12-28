package com.sky.vo.dish;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品列表页 VO
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id（行操作必需）</li>
 *   <li>只包含：列表页展示字段</li>
 * </ul>
 * 
 * <p>根据前端列表页，展示：菜品名称、图片、菜品分类、售价、售卖状态、最后操作时间
 */
@Data
public class DishPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜品ID（必须，用于行操作） */
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 图片 */
    private String image;

    /** 分类名称（列表展示用，非categoryId） */
    private String categoryName;

    /** 菜品价格 */
    private BigDecimal price;

    /** 状态：0-停售 1-起售 */
    private Integer status;

    /** 最后操作时间 */
    private LocalDateTime updateTime;

    // 注意：已移除categoryId，列表页只需展示categoryName
}