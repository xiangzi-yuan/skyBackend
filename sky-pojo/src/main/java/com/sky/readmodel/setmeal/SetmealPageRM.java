package com.sky.readmodel.setmeal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetmealPageRM {
    private Long id;
    private String name;
    private String image;
    private String categoryName;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime updateTime;
}
