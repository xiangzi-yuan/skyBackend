package com.sky.vo.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品口味 VO - 返回给前端的口味数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    //口味名称
    private String name;

    //口味数据list
    private String value;

}
