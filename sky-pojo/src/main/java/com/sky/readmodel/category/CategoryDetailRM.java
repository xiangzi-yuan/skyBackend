package com.sky.readmodel.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类详情/编辑回显 ReadModel - 用于数据库查询结果映射
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id</li>
 *   <li>必须包含：编辑保存所需的关键业务字段</li>
 *   <li>可包含：页面展示需要的派生字段</li>
 * </ul>
 * 
 * <p>根据前端修改弹窗，需要回显：分类名称、排序
 * <p>注意：type字段虽然不可编辑，但可能需要在详情展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDetailRM {

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

    /**
     * 状态：0-禁用 1-启用（用于业务校验）
     */
    private Integer status;
}
