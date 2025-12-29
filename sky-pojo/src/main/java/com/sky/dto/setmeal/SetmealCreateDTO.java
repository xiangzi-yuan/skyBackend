package com.sky.dto.setmeal;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SetmealCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @NotNull(message = ValidationMessageConstant.SETMEAL_CATEGORY_REQUIRED)
    private Long categoryId;

    /** 套餐名称 */
    @NotBlank(message = ValidationMessageConstant.SETMEAL_NAME_REQUIRED)
    private String name;

    /** 套餐价格 */
    @NotNull(message = ValidationMessageConstant.SETMEAL_PRICE_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessageConstant.SETMEAL_PRICE_MIN)
    private BigDecimal price;

    // 注意：status 字段已移除，新建套餐默认停售，由后端控制

    /** 描述信息 */
    private String description;

    /** 图片 */
    @NotBlank(message = ValidationMessageConstant.IMAGE_REQUIRED)
    private String image;

    /** 套餐菜品关系 */
    private List<SetmealDishDTO> setmealDishes;
}
