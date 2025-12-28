package com.sky.dto.dish;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishUpdateDTO implements Serializable {

    private Long id; // 必须要
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
    // private Integer status; // 修改时不修改状态, 转换器也不用忽略
    private List<DishFlavorDTO> flavors;

}