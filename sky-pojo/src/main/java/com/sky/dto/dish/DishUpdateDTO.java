package com.sky.dto.dish;

import com.sky.constant.ValidationMessageConstant;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishUpdateDTO implements Serializable {

    @NotNull(message = ValidationMessageConstant.ID_REQUIRED)
    private Long id; // 必须要
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    // private Integer status; // 修改时不修改状态, 转换器也不用忽略
    private List<DishFlavorDTO> flavors;

}
