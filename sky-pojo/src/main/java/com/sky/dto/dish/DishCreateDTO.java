package com.sky.dto.dish;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜品名称 */
    @NotBlank(message = ValidationMessageConstant.DISH_NAME_REQUIRED)
    private String name;

    /** 菜品分类ID */
    @NotNull(message = ValidationMessageConstant.DISH_CATEGORY_REQUIRED)
    private Long categoryId;

    /** 菜品价格 */
    @NotNull(message = ValidationMessageConstant.DISH_PRICE_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessageConstant.DISH_PRICE_MIN)
    private BigDecimal price;

    /** 图片 */
    @NotBlank(message = ValidationMessageConstant.IMAGE_REQUIRED)
    private String image;

    /** 描述信息 */
    private String description;

    // 注意：status 字段已移除，新建菜品默认停售，由后端控制

    /** 口味列表 */
    private List<DishFlavorDTO> flavors;
}
