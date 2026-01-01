package com.sky.vo.setmeal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户端套餐列表展示VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    //套餐名称
    private String name;
    //套餐价格
    private BigDecimal price;
    //描述信息
    private String description;
    //图片
    private String image;
    //月销量（暂未实现）
    //private Integer monthlySales;
}
