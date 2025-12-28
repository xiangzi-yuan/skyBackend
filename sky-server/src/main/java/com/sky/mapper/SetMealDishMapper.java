package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    /**
     * 查询菜品列表是否被任何套餐包含
     */
    //没写 @Param("dishIds")，xml 的 collection 通常要写 list
    List<Long> getSetmealIdsByDishIds(@Param("dishIds") List<Long> dishIds);

    /**
     * 根据菜品ID列表查询关联的套餐名称（用于删除时的错误提示）
     */
    List<String> getSetmealNamesByDishIds(@Param("dishIds") List<Long> dishIds);

    /**
     * 根据菜品ID列表查询菜品与套餐的关联关系（用于删除时的详细错误提示）
     */
    List<com.sky.vo.dish.DishSetmealRelationVO> getDishSetmealRelations(@Param("dishIds") List<Long> dishIds);

    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     *
     * @param setmealId 套餐id
     * @return List<SetmealDish>
     */
        @Select("""
                        select
                            id           as id,
                            setmeal_id   as setmealId,
                            dish_id      as dishId,
                            name         as name,
                            price        as price,
                            copies       as copies
                        from setmeal_dish 
                        where setmeal_id = #{setmealId}
                        """)
        List<SetmealDish> selectBySetmealId(Long setmealId);
}
