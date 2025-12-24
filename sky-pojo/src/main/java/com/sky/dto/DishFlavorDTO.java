package com.sky.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class DishFlavorDTO implements Serializable {

    private Long id;

    // 口味名称，如 "辣度"
    private String name;

    // 口味可选值（JSON字符串），如 ["微辣","中辣"]
    private String value;
}

