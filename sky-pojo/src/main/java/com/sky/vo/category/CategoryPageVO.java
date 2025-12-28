package com.sky.vo.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类列表页 VO - 返回给前端的列表数据
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id（行操作必需）</li>
 *   <li>只包含：列表页展示字段（同 PageRM）</li>
 * </ul>
 * 
 * <p>根据前端列表页，展示：分类名称、分类类型、排序、状态、操作时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（必须，用于行操作）
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 类型：1-菜品分类 2-套餐分类
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 最后更新时间（列表显示"操作时间"）
     */
    private LocalDateTime updateTime;
}
