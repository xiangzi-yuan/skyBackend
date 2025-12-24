package com.sky.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DishPageVO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private String image;
    private Integer status;
    private LocalDateTime updateTime;
    // 与rm一致
}