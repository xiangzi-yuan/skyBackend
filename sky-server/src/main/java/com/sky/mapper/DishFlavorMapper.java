package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入菜品口味
     * @param flavors 口味列表
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID删除口味（用于修改菜品时）
     * @param dishId 菜品ID
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
    
    /**
     * 根据菜品ID查询口味列表
     * @param dishId 菜品ID
     * @return 口味列表
     */
    @Select("select id, dish_id, name, value from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);
}