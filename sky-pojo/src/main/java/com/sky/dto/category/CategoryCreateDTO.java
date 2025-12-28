package com.sky.dto.category;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分类新增 DTO
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须不包含：id（数据库自动生成）</li>
 *   <li>必须不包含：createTime/updateTime/createUser/updateUser/isDeleted（系统字段）</li>
 *   <li>必须不包含：status（后端控制，新建默认禁用）</li>
 *   <li>可包含：页面提交的业务字段</li>
 * </ul>
 */
@Data
public class CategoryCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型：1-菜品分类 2-套餐分类
     * 由前端按钮决定（新增菜品分类/新增套餐分类）
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;
}
