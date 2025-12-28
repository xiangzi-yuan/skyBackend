package com.sky.vo.setmeal;

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
public class SetmealPageVO {
    private Long id; // 必须 用于修改等功能识别id
    private String name;
    private String image;
    private String categoryName;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime updateTime;
}
