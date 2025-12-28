package com.sky.vo.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分类详情/编辑回显 VO - 返回给前端的详情数据
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id</li>
 *   <li>必须包含：编辑保存需要的字段</li>
 *   <li>可包含：页面展示需要的只读字段</li>
 * </ul>
 * 
 * <p>根据前端修改弹窗，回显：分类名称、排序
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（必须）
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 类型：1-菜品分类 2-套餐分类（只读展示）
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    // 注意：已移除status，编辑回显只需要可编辑字段（name、sort）
}
