package com.sky.dto.category;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分类修改 DTO
 * 
 * <p>规范说明：
 * <ul>
 *   <li>必须包含：id（采用方案B：PUT /resource + body含id）</li>
 *   <li>必须不包含：createTime/updateTime/createUser/updateUser/isDeleted（系统字段）</li>
 *   <li>必须不包含：status（由单独的启用/禁用接口管理）</li>
 *   <li>必须不包含：type（分类类型创建后不可修改）</li>
 *   <li>可包含：允许修改的字段</li>
 * </ul>
 * 
 * <p>根据前端修改分类弹窗，只允许修改：分类名称、排序
 */
@Data
public class CategoryUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（必须）
     */
    @NotNull(message = ValidationMessageConstant.CATEGORY_ID_REQUIRED)
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = ValidationMessageConstant.CATEGORY_NAME_REQUIRED)
    private String name;

    /**
     * 排序
     */
    @NotNull(message = ValidationMessageConstant.SORT_REQUIRED)
    private Integer sort;
}
