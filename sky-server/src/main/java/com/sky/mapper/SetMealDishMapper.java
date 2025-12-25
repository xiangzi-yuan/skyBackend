package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetMealDishMapper {


    /**
     * 查询菜品列表是否被任何套餐包含
     */
    //没写 @Param("dishIds")，xml 的 collection 通常要写 list
    List<Long> getSetMealIdsByDishIds(@Param("dishIds") List<Long> dishIds);

}
